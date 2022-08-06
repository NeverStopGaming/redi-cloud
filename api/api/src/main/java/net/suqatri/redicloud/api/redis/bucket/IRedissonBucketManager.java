package net.suqatri.redicloud.api.redis.bucket;

import net.suqatri.redicloud.api.redis.IRedissonManager;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;

public interface IRedissonBucketManager<T extends I, I extends IRBucketObject> extends IRedissonManager {

    Class<T> getImplClass();

    FutureAction<I> getAsync(String identifier);
    I get(String identifier);

    FutureAction<T> getImplAsync(String identifier);
    T getImpl(String identifier);

    boolean isCached(String identifier);

    I getCached(String identifier);
    T getCachedImpl(String identifier);

    void removeCache(String identifier);

    FutureAction<I> createBucketAsync(String identifier, I object);
    I createBucket(String identifier, I object);

    boolean existsBucket(String identifier);
    FutureAction<Boolean> existsBucketAsync(String identifier);

    Collection<I> getBucketHolders();
    FutureAction<Collection<I>> getBucketHoldersAsync();

    boolean deleteBucket(String identifier);
    FutureAction<Boolean> deleteBucketAsync(String identifier);

    T publishChanges(T holder);
    FutureAction<T> publishChangesAsync(T holder);
}
