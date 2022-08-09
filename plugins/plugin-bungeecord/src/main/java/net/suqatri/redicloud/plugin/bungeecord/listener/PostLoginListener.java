package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

import java.util.concurrent.TimeUnit;

public class PostLoginListener implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event){
        if(BungeeCordCloudAPI.getInstance().getService().isMaintenance() && !event.getPlayer().hasPermission("redicloud.maintenance.bypass")){
            event.getPlayer().disconnect("§cThis proxy is currently under maintenance. Please try again later.");
            return;
        }

        if(event.getPlayer().getPendingConnection().isOnlineMode()) return;

        boolean isLoggedIn = false;

        if(BungeeCordCloudAPI.getInstance().getPlayerManager().existsPlayer(event.getPlayer().getUniqueId())){
            CloudPlayer cloudPlayer = (CloudPlayer) BungeeCordCloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
            isLoggedIn = cloudPlayer.isLoggedIn();
            cloudPlayer.setLastLogin(System.currentTimeMillis());
            cloudPlayer.setConnected(true);
            cloudPlayer.setLastIp(event.getPlayer().getPendingConnection().getAddress().getHostString());
            cloudPlayer.setLastConnectedProxyId(BungeeCordCloudAPI.getInstance().getService().getUniqueId());
            cloudPlayer.updateAsync();
        }

        if(!isLoggedIn){
            ICloudService cloudService = BungeeCordCloudAPI.getInstance().getPlayerManager().getVerifyService();
            if(cloudService == null){
                event.getPlayer().disconnect("§cThere is no verify service available. Please try again later.");
                return;
            }
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(cloudService.getServiceName());
            if(serverInfo == null){
                event.getPlayer().disconnect("§cThe verify service is currently unavailable. Please try again later.");
                CloudAPI.getInstance().getConsole().warn("The service " + cloudService.getServiceName() + " is not registered!");
                return;
            }
            event.getPlayer().connect(serverInfo);
        }else {
            event.getPlayer().sendMessage("You are logged in as " + event.getPlayer().getName() + "!");
        }
    }

}
