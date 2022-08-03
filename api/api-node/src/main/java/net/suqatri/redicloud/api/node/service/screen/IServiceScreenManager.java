package net.suqatri.redicloud.api.node.service.screen;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface IServiceScreenManager {

    IServiceScreen getServiceScreen(IRBucketHolder<ICloudService> serviceHolder);

    FutureAction<IServiceScreen> join(IServiceScreen serviceScreen);

    void leave(IServiceScreen serviceScreen);

    boolean isActive(IServiceScreen serviceScreen);

    boolean isActive(UUID serviceId);

    Collection<IServiceScreen> getActiveScreens();

    boolean isAnyScreenActive();

    void write(String command);

}
