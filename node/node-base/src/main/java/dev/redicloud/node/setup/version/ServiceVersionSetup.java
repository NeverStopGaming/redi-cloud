package dev.redicloud.node.setup.version;

import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupHeaderBehaviour;
import dev.redicloud.node.console.setup.annotations.Question;
import dev.redicloud.node.console.setup.annotations.RequiresEnum;
import lombok.Getter;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.node.NodeLauncher;

@Getter
public class ServiceVersionSetup extends Setup<ServiceVersionSetup> {

    @Question(id = 1, question = "What is the environment type of this version?")
    @RequiresEnum(ServiceEnvironment.class)
    private ServiceEnvironment environment;

    @Question(id = 2, question = "What is the download url of this version?")
    private String downloadUrl;

    @Question(id = 3, question = "Is this version a paper clip?")
    private boolean paperClip;

    @Question(id = 4, question = "What is the java command of this version?")
    private String javaCommand;

    public ServiceVersionSetup() {
        super(NodeLauncher.getInstance().getConsole());
    }

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.RESTORE_PREVIOUS_LINES;
    }
}
