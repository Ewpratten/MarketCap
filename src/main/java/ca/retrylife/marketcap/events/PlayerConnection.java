package ca.retrylife.marketcap.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ca.retrylife.marketcap.events.utils.PlayerSearch;

public class PlayerConnection implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        PlayerSearch.searchPlayer(event.getPlayer());
        System.out.println("Player logged in and searched");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerSearch.searchPlayer(event.getPlayer());
        System.out.println("Player logged out and searched");
    }

}