package net.suqatri.redicloud.api.node.service.factory;

import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.File;

public interface ICloudServiceProcess {

    ICloudService getService();

    void executeCommand(String command);

    boolean start() throws Exception;

    FutureAction<Boolean> stopAsync(boolean force);

    boolean isActive();

    File getServiceDirectory();

    int getPort();

    void setPort(int port);

}
