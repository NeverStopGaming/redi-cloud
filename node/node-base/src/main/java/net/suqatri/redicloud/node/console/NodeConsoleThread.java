package net.suqatri.redicloud.node.console;

import net.suqatri.commands.RootCommand;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.console.LogLevel;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.node.NodeLauncher;
import org.jline.reader.UserInterruptException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

public class NodeConsoleThread extends Thread {

    private final NodeConsole nodeConsole;

    public NodeConsoleThread(NodeConsole nodeConsole, String name) {
        super(name + "-console-thread");
        this.nodeConsole = nodeConsole;
    }

    @Override
    public void run() {
        String line;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                line = this.nodeConsole.getLineReader().readLine(this.nodeConsole.getPrefix());
                handleInput(line);
            }
        } catch (UserInterruptException e) {
            CloudAPI.getInstance().shutdown(false);
        } catch (Exception e1) {
            if (e1 instanceof ClassNotFoundException) {
                this.nodeConsole.log(LogLevel.FATAL, "Its seems that something overrides the node jar file! Please only override the node jar file when the node is not running!");
                this.nodeConsole.fatal("Node is crashed!", e1);
                CloudAPI.getInstance().shutdown(false);
            } else {
                this.nodeConsole.error("Error while reading console line!", e1);
            }
        }
    }

    public void handleInput(String raw) {

        String line = raw;

        if (line == null) return;
        if (line.isEmpty()) return;

        while (line.startsWith(" ")) {
            line = line.substring(1);
        }

        boolean isSetup = this.nodeConsole.getCurrentSetup() != null;

        for (Consumer<String> inputHandler : new ArrayList<>(this.nodeConsole.getInputHandler())) {
            inputHandler.accept(line);
        }

        if (isSetup) return;

        if (NodeLauncher.getInstance().getScreenManager().isAnyScreenActive()) {
            if (raw.equalsIgnoreCase("exit") || raw.equalsIgnoreCase("leave")) {
                for (IServiceScreen activeScreen : NodeLauncher.getInstance().getScreenManager().getActiveScreens()) {
                    NodeLauncher.getInstance().getScreenManager().leave(activeScreen);
                }
                return;
            }
        }

        String logLine = this.nodeConsole.getPrefix() + raw;
        String dateTime = java.time.format.DateTimeFormatter.ofPattern("dd-MM HH:mm:ss:SSS").format(java.time.LocalDateTime.now());
        String p = "[" + dateTime + "] ";
        if(this.nodeConsole.getFileHandler() != null) this.nodeConsole.getFileHandler().publish(new LogRecord(Level.INFO, p + logLine + "\n"));

        this.nodeConsole.getLineEntries().add(new ConsoleInput(line, System.currentTimeMillis(), this.nodeConsole.getPrefix()));

        String name = line.split(" ")[0];
        String[] args = line.split(" ");
        if (args.length > 1) {
            String[] args1 = new String[args.length - 1];
            System.arraycopy(args, 1, args1, 0, args1.length);
            args = args1;
        } else {
            args = new String[0];
        }

        List<RootCommand> commands = this.nodeConsole.getConsoleManager().getRegisteredRootCommands().parallelStream()
                .filter(rootCommand -> rootCommand.getCommandName().startsWith(name))
                .collect(Collectors.toList());

        for (RootCommand command : commands) {
            if (command.getCommandName().equalsIgnoreCase(name)) {
                command.execute(this.nodeConsole.getConsoleManager().getCommandIssuer(this.nodeConsole.getConsoleManager().getCommandSender()), name, args);
                return;
            }
        }
        this.nodeConsole.log(new ConsoleLine("COMMAND", "%tcUnknown command: %hc" + raw));
    }
}
