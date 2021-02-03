package ca.retrylife.marketcap.events;

import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.DoubleChestInventory;

import ca.retrylife.marketcap.database.DatabaseAPI;
import ca.retrylife.marketcap.events.utils.PlayerSearch;
import ca.retrylife.marketcap.util.HashUtil;
import io.sentry.Sentry;

public class InventoryInteraction implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        try {

            // Only pay attention to players
            if (event.getPlayer() instanceof Player) {

                // Search the player
                PlayerSearch.searchPlayer((Player) event.getPlayer());

                // Check the upper inventory
                if (event.getInventory() != event.getPlayer().getInventory()) {

                    if (event.getView().getTopInventory() instanceof Container) {

                        // Get the block hash
                        String hash = HashUtil.getContainerHash((Container) event.getInventory());

                        // Search the inventory
                        DatabaseAPI.getInstance().updateInventory(event.getInventory(), hash);

                    }

                }

            }

        } catch (Exception e) {
            Sentry.captureException(e);
            throw e;
        }

    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {
        try {

            // Only pay attention to players
            if (event.getEntity() instanceof Player) {
                PlayerSearch.searchPlayer((Player) event.getEntity());
            }
            
        } catch (Exception e) {
            Sentry.captureException(e);
            throw e;
        }

    }

}