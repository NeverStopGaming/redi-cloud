package net.suqatri.redicloud.node.service.factory;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.impl.service.packet.stop.CloudServiceInitStopPacket;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.event.CloudServiceStoppedEvent;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.StreamUtils;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.ConsoleLine;
import net.suqatri.redicloud.node.service.NodeCloudServiceManager;
import net.suqatri.redicloud.node.service.screen.packet.ScreenDestroyPacket;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
public class CloudServiceProcess implements ICloudServiceProcess {

    private final NodeCloudServiceFactory factory;
    private final CloudService serviceHolder;
    private File serviceDirectory;
    private Process process;
    private int port;
    private Thread thread;
    private final FutureAction<Boolean> stopFuture = new FutureAction<>();
    private IServiceScreen screen;

    @Override
    public void executeCommand(String command) {
        CloudAPI.getInstance().getServiceManager().executeCommand(this.serviceHolder, command);
    }

    @Override
    public boolean start() throws Exception {

        this.serviceDirectory = new File(this.serviceHolder.isStatic()
                ? Files.STATIC_SERVICE_FOLDER.getFile()
                : Files.TEMP_SERVICE_FOLDER.getFile(),
                this.serviceHolder.getServiceName() + "-" + this.serviceHolder.getUniqueId());
        if (!this.serviceDirectory.exists()) this.serviceDirectory.mkdirs();

        this.factory.getPortManager().getUnusedPort(this).get(5, TimeUnit.SECONDS);

        CloudServiceCopier copier = new CloudServiceCopier(this, CloudAPI.getInstance().getServiceTemplateManager());
        copier.copyFiles();

        CloudAPI.getInstance().getConsole().debug("Starting cloud service process " + this.serviceHolder.getServiceName() + " on port " + this.port);

        ProcessBuilder builder = new ProcessBuilder();
        Map<String, String> environment = builder.environment();
        environment.put("redicloud_service_id", this.getService().getUniqueId().toString());
        environment.put("redicloud_path", NodeLauncher.getInstance().getNode().getFilePath());
        environment.put("redicloud_log_level", NodeLauncher.getInstance().getConsole().getLogLevel().name());
        for (Files value : Files.values()) {
            environment.put("redicloud_files_" + value.name().toLowerCase(), value.getFile().getAbsolutePath());
        }
        builder.directory(this.serviceDirectory);
        builder.command(getStartCommand(this.serviceHolder.getServiceVersion().get(3, TimeUnit.SECONDS)));
        CloudAPI.getInstance().getConsole().debug("Start command: " + builder.command().parallelStream().collect(Collectors.joining(" ")));
        this.process = builder.start();

        this.serviceHolder.setServiceState(ServiceState.STARTING);
        this.serviceHolder.setMaxRam(this.serviceHolder.getConfiguration().getMaxMemory());
        this.serviceHolder.setHostName(NodeLauncher.getInstance().getNode().getHostname());
        this.serviceHolder.setPort(this.port);
        this.serviceHolder.update();

        NodeLauncher.getInstance().getNode().setMemoryUsage(NodeLauncher.getInstance().getNode().getMemoryUsage()
                + this.serviceHolder.getConfiguration().getMaxMemory());
        NodeLauncher.getInstance().getNode().update();

        this.thread = new Thread(() -> {
            try {
                RateLimiter rate = RateLimiter.create(30, 5, TimeUnit.SECONDS);
                screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(this.serviceHolder);
                InputStreamReader inputStreamReader = new InputStreamReader(this.process.getInputStream());
                BufferedReader reader = new BufferedReader(inputStreamReader);
                while (
                        this.process.isAlive()
                                && Thread.currentThread().isAlive()
                                && !Thread.currentThread().isInterrupted()
                                && StreamUtils.isOpen(this.process.getInputStream())
                ) {
                    try {
                        String line = reader.readLine();
                        if (line == null) continue;
                        if (line.isEmpty() || line.equals(" ") || line.contains("InitialHandler has pinged"))
                            continue; //"InitialHandler has pinged" for ping flood protection
                        rate.acquire();
                        screen.addLine(line);
                    } catch (IOException e) {
                        //stream closed...
                    }
                }
                CloudAPI.getInstance().getConsole().trace("Closed stream for service " + this.serviceHolder.getServiceName());

                NodeLauncher.getInstance().getNode().setMemoryUsage(NodeLauncher.getInstance().getNode().getMemoryUsage()
                        - this.serviceHolder.getConfiguration().getMaxMemory());
                NodeLauncher.getInstance().getNode().update();

                reader.close();

                this.destroyScreen();

                this.factory.getPortManager().unUsePort(this);

                CloudAPI.getInstance().getEventManager().postGlobalAsync(new CloudServiceStoppedEvent(this.serviceHolder));

                if (!this.serviceHolder.isStatic())
                    ((NodeCloudServiceManager) this.factory.getServiceManager())
                            .deleteBucket(this.serviceHolder.getUniqueId().toString());

                if (StreamUtils.isOpen(this.process.getErrorStream())) {
                    CloudAPI.getInstance().getConsole().trace("Read error stream for service " + this.serviceHolder.getServiceName());
                    reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
                    while (
                            StreamUtils.isOpen(this.process.getErrorStream())
                            && Thread.currentThread().isAlive()
                            && !Thread.currentThread().isInterrupted()
                            && reader.ready()
                    ) {
                        String line = reader.readLine();
                        if (line == null) continue;
                        CloudAPI.getInstance().getConsole().log(new ConsoleLine("SCREEN-ERROR [" + this.serviceHolder.getServiceName() + "]", line));
                    }
                    CloudAPI.getInstance().getConsole().trace("Closed error stream for service " + this.serviceHolder.getServiceName());
                    reader.close();
                }

                if (this.serviceDirectory.exists() && !this.serviceHolder.isStatic()) {
                    FileUtils.deleteDirectory(this.serviceDirectory);
                }
                CloudAPI.getInstance().getConsole().debug("Cloud service process " + this.serviceHolder.getServiceName() + " has been stopped");

                CloudAPI.getInstance().getConsole().trace("Call stopping future action: " + this.stopFuture + " for service " + this.serviceHolder.getServiceName());
                if (!this.stopFuture.isFinishedAnyway()) {
                    this.stopFuture.complete(true);
                }

            } catch (Exception e) {

                this.stopFuture.completeExceptionally(e);
                CloudAPI.getInstance().getConsole().error("Cloud service process " + this.serviceHolder.getServiceName() + " has been stopped exceptionally!", e);

                this.destroyScreen();
                this.factory.getPortManager().unUsePort(this);
                if (!this.serviceHolder.isStatic()) {
                    ((NodeCloudServiceManager) this.factory.getServiceManager()).deleteBucket(this.serviceHolder.getUniqueId().toString());
                    CloudAPI.getInstance().getServiceManager().removeFromFetcher(this.serviceHolder.getServiceName());
                } else {
                    this.serviceHolder.setServiceState(ServiceState.OFFLINE);
                    this.serviceHolder.updateAsync();
                }

                if (this.serviceDirectory.exists() && !this.serviceHolder.isStatic()) {
                    try {
                        FileUtils.deleteDirectory(this.serviceDirectory);
                    } catch (IOException e1) {
                        CloudAPI.getInstance().getConsole().error("Temp service directory of " + this.serviceHolder.getServiceName() + " cannot be deleted (" + this.serviceDirectory.getAbsolutePath() + ")", e1);
                    }
                }

            }
        }, "redicloud-service-" + this.serviceHolder.getServiceName());
        this.thread.start();

        return true;
    }

    private void destroyScreen() {
        if (this.screen == null) return;
        ScreenDestroyPacket screenDestroyPacket = null;
        for (UUID nodeId : this.serviceHolder.getConsoleNodeListenerIds()) {
            if (nodeId.equals(NodeLauncher.getInstance().getNode().getUniqueId())) continue;
            ICloudNode node = CloudAPI.getInstance().getNodeManager().getNode(nodeId);
            if (screenDestroyPacket == null) {
                screenDestroyPacket = new ScreenDestroyPacket();
                screenDestroyPacket.setServiceId(this.serviceHolder.getUniqueId());
            }
            screenDestroyPacket.getPacketData().addReceiver(node.getNetworkComponentInfo());
        }
        if (screenDestroyPacket != null) {
            screenDestroyPacket.publishAsync();
        }
        this.screen.deleteLines();
        this.screen = null;
    }

    @Override
    public FutureAction<Boolean> stopAsync(boolean force) {
        this.stopProcess(force);

        return this.stopFuture;
    }

    @Override
    public boolean isActive() {
        return this.process != null && this.process.isAlive();
    }

    public void deleteTempFiles(boolean force) throws IOException {
        if (isActive()) stopProcess(force);
        if (this.serviceHolder.isStatic() || !this.serviceDirectory.exists()) return;
        FileUtils.deleteDirectory(this.serviceDirectory);
    }

    public FutureAction<Boolean> deleteTempFilesAsync(boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if (isActive()) {
            stopProcess(force);
        }

        if (this.serviceHolder.isStatic() || !this.serviceDirectory.exists()) {
            futureAction.complete(true);
            return futureAction;
        }

        CloudAPI.getInstance().getExecutorService().submit(() -> {
            try {
                FileUtils.deleteDirectory(this.serviceDirectory);
                futureAction.complete(true);
            } catch (IOException e) {
                futureAction.completeExceptionally(e);
            }
        });

        return futureAction;
    }

    public void stopProcess(boolean force) {

        this.factory.getPortManager().unUsePort(this);

        if (!isActive()) {
            if (!this.stopFuture.isFinishedAnyway()) {
                this.stopFuture.complete(true);
            }
            return;
        }
        if (force) {
            this.process.destroy();
        } else {
            CloudServiceInitStopPacket packet = new CloudServiceInitStopPacket();
            packet.getPacketData().addReceiver(this.serviceHolder.getNetworkComponentInfo());
            packet.publishAsync();
            CloudAPI.getInstance().getScheduler().runTaskLaterAsync(() -> { // service crashed, force stop
                if(this.serviceHolder.getServiceState() == ServiceState.RUNNING_DEFINED
                        || this.serviceHolder.getServiceState() == ServiceState.RUNNING_UNDEFINED) {
                    this.process.destroy();
                }
            }, 1500, TimeUnit.MILLISECONDS);
        }
    }

    private List<String> getStartCommand(ICloudServiceVersion serviceVersionHolder) {
        List<String> command = new ArrayList<>();

        command.add(serviceVersionHolder.getJavaCommand());

        command.addAll(this.serviceHolder.getConfiguration().getJvmArguments());


        command.add("-Xms" + this.serviceHolder.getConfiguration().getMaxMemory() + "M");
        command.add("-Xmx" + this.serviceHolder.getConfiguration().getMaxMemory() + "M");


        if (this.serviceHolder.getEnvironment() == ServiceEnvironment.MINECRAFT) {
            command.add("-Dcom.mojang.eula.agree=true");
            command.add("-Djline.terminal=jline.UnsupportedTerminal");
        }

        command.add("-jar");
        command.add(this.serviceDirectory.getAbsolutePath() + File.separator + "service.jar");

        if (this.serviceHolder.getEnvironment() == ServiceEnvironment.MINECRAFT) {
            command.add("nogui");
        }

        command.addAll(this.serviceHolder.getConfiguration().getProcessParameters());

        return command;
    }
}
