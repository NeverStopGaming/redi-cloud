package dev.redicloud.node.console;

import dev.redicloud.commands.CommandSender;

public class NodeConsoleSender extends CommandSender {

    private CommandConsoleManager consoleManager;

    public NodeConsoleSender(CommandConsoleManager consoleManager) {
        super(consoleManager.getNodeConsole().getPrefix());
        this.consoleManager = consoleManager;
    }

    @Override
    public void sendMessage(String message) {
        this.consoleManager.getNodeConsole().log(new ConsoleLine("COMMAND", "%tc" + message));
    }
}
