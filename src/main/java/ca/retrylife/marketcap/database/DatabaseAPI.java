package ca.retrylife.marketcap.database;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ca.retrylife.marketcap.util.SentryUtil;
import io.sentry.SentryLevel;
import redis.clients.jedis.Jedis;
import com.github.tonivade.claudb.ClauDB;
import com.github.tonivade.resp.RespServer;

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

    // Redis
    private static final int REDIS_DB_PORT = 6370;
    // private RedisServer server;
    private RespServer server;
    private Jedis client;

    // Hidden constructor to force singleton usage
    private DatabaseAPI() {

        // Crash tracking
        SentryUtil.breadcrumb(getClass(), "Instantiated DatabaseAPI Singleton");

        // Start redis server
        server = ClauDB.builder().host("localhost").port(REDIS_DB_PORT).build();

    }

    public void startServer() {

        // Crash tracking
        SentryUtil.breadcrumb(getClass(), "Starting Redis");

        // Start the server
        server.start();

        // Connect to self
        client = new Jedis("localhost", REDIS_DB_PORT);
    }

    public void stopServer() {

        // Crash tracking
        SentryUtil.breadcrumb(getClass(), "Stopping Redis");

        // Stop server
        server.stop();

        // Stop the client
        client.close();
    }

    public void updateInventory(Inventory inventory, String ownerHash) {

        // Get a list of all items to be tracked
        String trackingList = client.get("tracked_items");
        if (trackingList == null) {
            return;
        }
        String[] itemsToTrack = trackingList.split(",");

        // Build a LUT of each item found
        Map<String, Integer> itemLUT = new HashMap<String, Integer>();

        // Loop through the inventory
        for (ItemStack stack : inventory) {

            // Skip if null
            if (stack == null) {
                continue;
            }

            // Check if this stack is any of the items to track
            for (String item : itemsToTrack) {

                // If there is a type match
                if (stack.getType().toString().equals(item)) {

                    // Build the name of this inventory key
                    String key = String.format("inventory_%s_%s", ownerHash, item);

                    // Handle incrementing the count in the Map
                    itemLUT.compute(key, (k, value) -> {
                        if (value == null) {
                            return 1;
                        } else {
                            return value + 1;
                        }
                    });
                }

            }

            // Write every inventory tally to redis
            for (String key : itemLUT.keySet()) {
                client.set(key, itemLUT.get(key).toString());
            }

        }

    }

    public void enableTracking(Material mat, CommandSender sender) {

        // Get the current tracking list
        String trackingList = client.get("tracked_items");

        // Get the material name
        String matName = mat.toString();

        // Handle key creation vs appending
        trackItem: {
            if (trackingList == null) {

                // Add the item
                client.set("tracked_items", matName);
                SentryUtil.breadcrumb(getClass(), "Added first item to tracking list");

            } else {

                // Check for this item already existing
                String[] trackingContents = trackingList.split(",");
                for (String item : trackingContents) {
                    if (item.equals(matName)) {
                        sender.sendMessage(String.format("Item already being tracked: %s", matName));
                        SentryUtil.breadcrumb(getClass(), "Tried tracking an item that is already tracked",
                                SentryLevel.WARNING);
                        break trackItem;
                    }
                }

                // Append the item
                client.set("tracked_items", String.format("%s,%s", trackingList, matName));
                SentryUtil.breadcrumb(getClass(), "Added new item to tracking list");

            }
            sender.sendMessage(String.format("Started tracking item: %s", matName));
        }

    }

    public void disableTracking(Material mat, CommandSender sender) {

        // Get the current tracking list
        String trackingList = client.get("tracked_items");

        // Get the material name
        String matName = mat.toString();

        // Handle key creation vs appending

        if (trackingList == null) {

            // Issue
            sender.sendMessage(String.format("Item not being tracked: %s", matName));
            SentryUtil.breadcrumb(getClass(), "Tried untracking an item, when no items are being tracked",
                    SentryLevel.WARNING);

        } else {

            // Check for this item already existing
            String[] trackingContents = trackingList.split(",");
            StringJoiner newOutput = new StringJoiner(",");
            for (String item : trackingContents) {
                if (!item.equals(matName)) {
                    newOutput.add(item);
                }
            }

            // Store the new list
            client.set("tracked_items", newOutput.toString());
            SentryUtil.breadcrumb(getClass(), "Removed item from tracking list");
            sender.sendMessage(String.format("Stopped tracking item: %s", matName));

        }

    }

    public Map<String, Integer> getMarketCapInformation() {
        
        // Build and output map
        Map<String, Integer> output = new HashMap<>();

        // Get every redis entry and convert
        for (String key : client.keys("inventory_*")) {
            output.put(key, Integer.parseInt(client.get(key)));
        }

        return output;

    }

}