package ca.retrylife.marketcap.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ca.retrylife.marketcap.events.utils.PlayerSearch;
import io.sentry.Sentry;

public class PlayerConnection implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            PlayerSearch.searchPlayer(event.getPlayer());
            System.out.println("Player logged in and searched for items");
        } catch (Exception e) {
            Sentry.captureException(e);
            throw e;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            PlayerSearch.searchPlayer(event.getPlayer());
            System.out.println("Player logged out and searched for items");
        } catch (Exception e) {
            Sentry.captureException(e);
            throw e;
        }
    }

}