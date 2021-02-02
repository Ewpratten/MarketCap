package ca.retrylife.marketcap.events.utils;

import org.bukkit.entity.Player;

import ca.retrylife.marketcap.database.DatabaseAPI;
import ca.retrylife.marketcap.util.HashUtil;

public class PlayerSearch {

    public static void searchPlayer(Player player) {
        // Get the player hashes
        String playerHash = HashUtil.getPlayerHash(player);
        String playerEndChestHash = HashUtil.getEndChestHash(player);

        // Track their inventories
        DatabaseAPI.getInstance().updateInventory(player.getInventory(), playerHash);
        DatabaseAPI.getInstance().updateInventory(player.getEnderChest(), playerEndChestHash);
    }

}