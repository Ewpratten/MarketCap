package ca.retrylife.marketcap.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        this.datastore = Preferences.systemNodeForPackage(getClass());

    }

    /**
     * Update all database entries for any given inventory
     * 
     * @param inventory Entity or block {@link Inventory}
     * @param ownerHash Hash of the {@link Inventory} owner
     */
    public void updateInventory(Inventory inventory, String ownerHash) {

        // Get the current tracking list
        MaterialList trackingList = new MaterialList(datastore.get(TRACKED_ITEMS_LIST_KEY, null));

        // Build an empty mapping to story tallies
        Map<String, Integer> itemTally = new HashMap<String, Integer>();

        // Search the inventory
        for (ItemStack itemStack : inventory) {
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

        // Write each tally
        itemTally.entrySet().forEach((entry) -> {

            // Construct a key name
            String key = String.format("%s%s%s%s%s", INVENTORY_TALLY_PREFIX_KEY, INVENTORY_INFO_SEPARATOR, ownerHash,
                    INVENTORY_INFO_SEPARATOR, entry.getKey());

            // Write
            datastore.put(key, entry.getValue().toString());

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

        // Build and output map
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

}