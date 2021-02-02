package ca.retrylife.marketcap.events;

import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import ca.retrylife.marketcap.database.DatabaseAPI;
import ca.retrylife.marketcap.events.utils.PlayerSearch;
import ca.retrylife.marketcap.util.HashUtil;

public class InventoryInteraction implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {

        // Only pay attention to players
        if (event.getPlayer() instanceof Player) {

            // Search the player
            PlayerSearch.searchPlayer((Player) event.getPlayer());

            // Check the upper inventory
            if (event.getInventory() != event.getPlayer().getInventory()) {

                // Only check blocks
                if (event.getInventory() instanceof Container) {

                    // Get the block hash
                    String hash = HashUtil.getContainerHash((Container) event.getInventory());

                    // Search the inventory
                    DatabaseAPI.getInstance().updateInventory(event.getInventory(), hash);

                }

            }

        }

    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {

        // Only pay attention to players
        if (event.getEntity() instanceof Player) {
            PlayerSearch.searchPlayer((Player) event.getEntity());
        }

    }

}