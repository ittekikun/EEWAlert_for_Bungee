package com.ittekikun.bplugin.eewalert;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;


public class PostLoginListener implements Listener
{
    private EEWAlert plugin;
    private EEWAlertConfig eewAlertConfig;

    public PostLoginListener(EEWAlert plugin)
    {
        this.plugin = plugin;
        this.eewAlertConfig = plugin.eewAlertConfig;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event)
    {
        ProxiedPlayer player = event.getPlayer();

        if(player.hasPermission("eewalert.bungee.vcheck"))
        {
            double NowVer = Double.valueOf(plugin.getProxy().getPluginManager().getPlugin("EEWAlert").getDescription().getVersion());

            String url = "EEWAlert_for_Bungee";

            Thread updateCheck = new Thread(new UpdateCheck(player,url,NowVer));
            updateCheck.start();
        }
    }
}
