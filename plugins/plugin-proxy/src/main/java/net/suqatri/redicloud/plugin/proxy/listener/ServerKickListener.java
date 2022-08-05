package net.suqatri.redicloud.plugin.proxy.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;

public class ServerKickListener implements Listener {

    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        IRBucketHolder<ICloudService> fallbackHolder = CloudAPI.getInstance().getServiceManager().getFallbackService();
        if (fallbackHolder == null) {
            event.getPlayer().disconnect("Fallback service is not available.");
            event.setCancelled(true);
            return;
        }
        event.getPlayer().sendMessage(event.getKickReasonComponent());
        event.setCancelServer(ProxyServer.getInstance().getServerInfo(fallbackHolder.get().getServiceName()));
    }

}
