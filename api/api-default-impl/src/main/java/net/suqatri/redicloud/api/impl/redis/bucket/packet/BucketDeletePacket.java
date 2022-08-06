package net.suqatri.redicloud.api.impl.redis.bucket.packet;

import lombok.Data;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;

@Data
public class BucketDeletePacket extends CloudPacket {

    private String identifier;
    private String redisPrefix;

    @Override
    public void receive() {
        RedissonBucketManager manager = RedissonBucketManager.getManager(this.redisPrefix);
        if (manager == null)
            throw new NullPointerException("RedissonBucketManager is null of prefix " + this.redisPrefix + "!");
        manager.getCachedBucketObjects().remove(this.identifier);
    }
}
