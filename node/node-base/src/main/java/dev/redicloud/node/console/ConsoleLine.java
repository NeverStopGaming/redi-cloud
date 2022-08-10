package dev.redicloud.node.console;

import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.console.IConsoleLine;
import dev.redicloud.api.console.LogLevel;

import java.io.Serializable;

@Getter
public class ConsoleLine implements IConsoleLine, Cloneable, Serializable {

    private final LogLevel logLevel;
    private final String prefix;
    private final String message;
    @Setter
    private long time = System.currentTimeMillis();
    private boolean stored = true;
    private boolean printTimestamp = true;
    private boolean printPrefix = true;
    private boolean logToFile = true;

    public ConsoleLine(LogLevel logLevel, String message) {
        this.logLevel = logLevel;
        this.message = message;
        this.prefix = logLevel.name();
    }

    public ConsoleLine(String prefix, String message) {
        this.logLevel = LogLevel.INFO;
        this.prefix = prefix;
        this.message = message;
    }

    @Override
    public IConsoleLine disableFileLogging() {
        this.logToFile = false;
        return this;
    }

    @Override
    public boolean printPrefix() {
        return this.printPrefix;
    }

    @Override
    public IConsoleLine setPrintPrefix(boolean printPrefix) {
        this.printPrefix = printPrefix;
        return this;
    }

    @Override
    public void println() {
        CloudAPI.getInstance().getConsole().log(this);
    }

    @Override
    public boolean printTimestamp() {
        return this.printTimestamp;
    }

    @Override
    public IConsoleLine setPrintTimestamp(boolean printTimestamp) {
        this.printTimestamp = printTimestamp;
        return this;
    }

    @Override
    public IConsoleLine setStored(boolean stored) {
        this.stored = stored;
        return this;
    }

    @Override
    public ConsoleLine clone() {
        try {
            ConsoleLine clone = (ConsoleLine) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
