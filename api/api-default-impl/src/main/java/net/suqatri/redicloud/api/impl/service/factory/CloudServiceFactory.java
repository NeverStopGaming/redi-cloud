package net.suqatri.redicloud.api.impl.service.factory;

import lombok.Data;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ICloudServiceManager;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

@Data
public class CloudServiceFactory implements ICloudServiceFactory {

    private final ICloudServiceManager cloudServiceManager;

    //TODO packets to node
    @Override
    public FutureAction<ICloudService> queueService(IServiceStartConfiguration configuration) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    //TODO packets to node
    @Override
    public FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
