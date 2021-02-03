package ca.retrylife.marketcap.events.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import ca.retrylife.marketcap.database.DatabaseAPI;
import ca.retrylife.marketcap.util.HashUtil;

/**
 * A utility for searching chunks for inventories
 */
public class ChunkySearcher {

    private static final int MAX_RADIUS = 3;

    // Location
    private final Location location;

    /**
     * Create a ChunkySearcher at a location
     * 
     * @param location Location to search
     */
    public ChunkySearcher(Location location) {
        this.location = location;
    }

    /**
     * Search a radius. WARNING: this will cause lag on the server
     * 
     * @param sender      Sender of the search command
     * @param chunkRadius Radius to search
     */
    public void search(CommandSender sender, int chunkRadius) {

        // Ensure the radius is reasonable
        if (chunkRadius > MAX_RADIUS && chunkRadius < 1) {
            sender.sendMessage(
                    String.format("Chunk radius %d is outside valid range [1, %d]", chunkRadius, MAX_RADIUS));
            return;
        }

        // Get the central block
        Block centre = this.location.getBlock();

        // Do an XYZ search
        for (int x = -(chunkRadius * 16); x <= chunkRadius * 16; x++) {
            for (int y = 0; y <= 255; y++) {
                for (int z = -(chunkRadius * 16); z <= chunkRadius * 16; z++) {

                    // Get the referenced block
                    Block curBlock = centre.getRelative(x, y, z);

                    // Check if this block is a container
                    if (curBlock.getState() instanceof InventoryHolder) {

                        // Get the block hash
                        String hash = HashUtil.getBlockHash(curBlock);

                        // Get block inventory
                        Inventory inventory = ((InventoryHolder) curBlock.getState()).getInventory();

                        // Search the inventory
                        DatabaseAPI.getInstance().updateInventory(inventory, hash);

                    }

                }
            }
        }
    }

}