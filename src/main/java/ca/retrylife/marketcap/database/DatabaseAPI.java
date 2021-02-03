package ca.retrylife.marketcap.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import ca.retrylife.marketcap.database.types.MaterialList;
import ca.retrylife.marketcap.util.SentryUtil;
import io.sentry.SentryLevel;

public class DatabaseAPI {

    // Internal instance reference
    private static DatabaseAPI instance = null;

    /**
     * Get the global instance of DatabaseAPI
     *
     * @return DatabaseAPI instance
     */
    public static DatabaseAPI getInstance() {
        if (instance == null) {
            instance = new DatabaseAPI();
        }
        return instance;
    }

    // Data storage
    private Preferences datastore;

    // Some common keys
    private static final String TRACKED_ITEMS_LIST_KEY = "tracked_items";
    private static final String INVENTORY_TALLY_PREFIX_KEY = "inventory";
    private static final String INVENTORY_INFO_SEPARATOR = ";";

    // Hidden constructor to force singleton usage
    private DatabaseAPI() {

        // Crash tracking
        SentryUtil.breadcrumb(getClass(), "Instantiated DatabaseAPI Singleton");

        // Connect to local datastore
        // We need to manually set a name here, since the plugin may be obfuscated
        this.datastore = Preferences.userRoot().node("ca_retrylife_marketcap_database_DatabaseAPI");

    }

    public void wipe() throws BackingStoreException {
        this.datastore.clear();
    }

    public void updateInventory(Inventory inventory, String ownerHash) {
        updateInventory(inventory, ownerHash, 1);
    }

    public Map<String, Integer> tallyItemsInInventory(Inventory inventory, MaterialList trackingList) {
        // Build an empty mapping to story tallies
        Map<String, Integer> itemTally = new HashMap<String, Integer>();

        // add a 0-count
        for (String materialName : trackingList.getList()) {
            itemTally.put(materialName, 0);
        }

        // Search the inventory
        for (ItemStack itemStack : inventory) {

            // Skip null items
            if (itemStack == null) {
                continue;
            }

            for (String materialName : trackingList.getList()) {

                // Search for a name match
                if (itemStack.getType().toString().equals(materialName)) {

                    // If needed, init the tally for this material
                    if (!itemTally.containsKey(materialName)) {
                        itemTally.put(materialName, 0);
                    }

                    // Get the existing tally
                    int tally = itemTally.get(materialName);

                    // Write the new tally
                    itemTally.put(materialName, tally + itemStack.getAmount());

                    // Break to jump to next slot in inventory
                    break;
                }

            }
        }
        return itemTally;
    }

    /**
     * Update all database entries for any given inventory
     * 
     * @param inventory Entity or block {@link Inventory}
     * @param ownerHash Hash of the {@link Inventory} owner
     */
    public void updateInventory(Inventory inventory, String ownerHash, int div) {

        // Get the current tracking list
        MaterialList trackingList = new MaterialList(datastore.get(TRACKED_ITEMS_LIST_KEY, null));

        Map<String, Integer> inventoryTopLevelTally = tallyItemsInInventory(inventory, trackingList);

        // Merge in every sub-inventory
        for (ItemStack itemStack : inventory) {

            // Handle null items
            if(itemStack == null){
                continue;
            }

            // Check for BlockInventoryHolder
            if (itemStack.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta im = (BlockStateMeta) itemStack.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulker = (ShulkerBox) im.getBlockState();

                    // Get the tally for the shulker
                    Map<String, Integer> tally = tallyItemsInInventory(shulker.getSnapshotInventory(), trackingList);

                    // Merge into the root inventory
                    inventoryTopLevelTally.putAll(tally);

                }
            }

        }

        // Write each tally
        inventoryTopLevelTally.entrySet().forEach((entry) -> {

            // Construct a key name
            String key = String.format("%s%s%s%s%s", INVENTORY_TALLY_PREFIX_KEY, INVENTORY_INFO_SEPARATOR, ownerHash,
                    INVENTORY_INFO_SEPARATOR, entry.getKey());

            // Write
            datastore.put(key, Integer.toString(entry.getValue() / div));

        });

    }

    /**
     * Enable tracking for a specific {@link Material}
     * 
     * @param mat    {@link Material} to track
     * @param sender The sender of this command
     */
    public void enableTracking(Material mat, CommandSender sender) {

        // Get the current tracking list
        MaterialList trackingList = new MaterialList(datastore.get(TRACKED_ITEMS_LIST_KEY, null));

        // Handle invalid call
        if (trackingList.contains(mat)) {
            sender.sendMessage(String.format("Item already being tracked: %s", mat.toString()));
            SentryUtil.breadcrumb(getClass(), "Tried tracking an item that is already being tracked",
                    SentryLevel.WARNING);
            return;
        }

        // Add to list
        trackingList.add(mat);

        // Write changes
        datastore.put(TRACKED_ITEMS_LIST_KEY, trackingList.toString());

        // Notify the client
        sender.sendMessage(String.format("Started tracking item: %s", mat.toString()));
        SentryUtil.breadcrumb(getClass(), "Added item to tracking list");

    }

    /**
     * Disable tracking for a specific {@link Material}
     * 
     * @param mat    {@link Material} to stop tracking
     * @param sender The sender of this command
     */
    public void disableTracking(Material mat, CommandSender sender) {

        // Get the current tracking list
        MaterialList trackingList = new MaterialList(datastore.get(TRACKED_ITEMS_LIST_KEY, null));

        // Handle invalid call
        if (!trackingList.contains(mat)) {
            sender.sendMessage(String.format("Item not being tracked: %s", mat.toString()));
            SentryUtil.breadcrumb(getClass(), "Tried untracking an item that is not being tracked",
                    SentryLevel.WARNING);
            return;
        }

        // Remove from list
        trackingList.remove(mat);

        // Write changes
        datastore.put(TRACKED_ITEMS_LIST_KEY, trackingList.toString());

        // Notify the client
        sender.sendMessage(String.format("Stopped tracking item: %s", mat.toString()));
        SentryUtil.breadcrumb(getClass(), "Removed item from tracking list");

    }

    /**
     * Get all market cap information as a map for printing
     * 
     * @return Map
     * @throws BackingStoreException
     */
    public Map<String, Integer> getMarketCapInformation() throws BackingStoreException {

        // Build an output map
        Map<String, Integer> output = new HashMap<>();

        // Search every db entry
        for (String key : datastore.keys()) {

            // Split the key into the information we need
            String[] keyComponents = key.split(INVENTORY_INFO_SEPARATOR);

            // Skip if this is not correctly formatted data
            if (keyComponents.length < 3 || !keyComponents[0].equals(INVENTORY_TALLY_PREFIX_KEY)) {
                continue;
            }

            // Get the material name
            String materialName = keyComponents[2];

            // Add an output entry if one does not exist
            if (!output.containsKey(materialName)) {
                output.put(materialName, 0);
            }

            // Get the tally for this material
            int tally = output.get(materialName);

            // Get this key's tally
            int thisTally = Integer.parseInt(datastore.get(key, "0"));

            // Add this tally
            output.put(materialName, tally + thisTally);

        }

        return output;

    }

    public Map<String, String> dumpDB() throws BackingStoreException {

        // Build an output map
        Map<String, String> output = new HashMap<>();

        // Search every db entry
        for (String key : datastore.keys()) {

            // Write the entry
            output.put(key, datastore.get(key, "null"));

        }

        return output;

    }

}