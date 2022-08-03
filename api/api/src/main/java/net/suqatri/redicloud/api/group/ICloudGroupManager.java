package net.suqatri.redicloud.api.group;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudGroupManager {

    FutureAction<IRBucketHolder<ICloudGroup>> getGroupAsync(UUID uniqueId);

    IRBucketHolder<ICloudGroup> getGroup(UUID uniqueId);

    FutureAction<IRBucketHolder<ICloudGroup>> getGroupAsync(String name);

    IRBucketHolder<ICloudGroup> getGroup(String name);

    FutureAction<Boolean> existsGroupAsync(UUID uniqueId);

    boolean existsGroup(UUID uniqueId);

    FutureAction<Boolean> existsGroupAsync(String name);

    boolean existsGroup(String name);

    Collection<IRBucketHolder<ICloudGroup>> getGroups();

    FutureAction<Collection<IRBucketHolder<ICloudGroup>>> getGroupsAsync();

    IRBucketHolder<ICloudGroup> createGroup(ICloudGroup group);

    FutureAction<IRBucketHolder<ICloudGroup>> createGroupAsync(ICloudGroup group);

    FutureAction<Boolean> deleteGroupAsync(UUID uniqueId);

    boolean deleteGroup(UUID uniqueId) throws Exception;

    FutureAction<IRBucketHolder<ICloudGroup>> addDefaultTemplates(IRBucketHolder<ICloudGroup> group);

}
