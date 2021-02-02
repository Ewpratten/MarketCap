package ca.retrylife.marketcap.database;

import java.io.IOException;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import ca.retrylife.marketcap.util.SentryUtil;
import io.sentry.Sentry;
import redis.embedded.RedisServer;

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
    private RedisServer server = null;

    // Hidden constructor to force singleton usage
    private DatabaseAPI() {

        // Crash tracking
        SentryUtil.breadcrumb(getClass(), "Instantiated DatabaseAPI Singleton");

        // Start redis server
        try {
            server = new RedisServer(REDIS_DB_PORT);
        } catch (IOException e) {
            Sentry.captureException(e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public void startServer() {

        // Crash tracking
        SentryUtil.breadcrumb(getClass(), "Starting Redis");

        // Start the server
        server.start();
    }

    public void stopServer() {

        // Crash tracking
        SentryUtil.breadcrumb(getClass(), "Stopping Redis");

        // Stop server
        server.stop();
    }

    public void updateInventory(Inventory inventory, String ownerHash) {

    }

    public void enableTracking(Material mat) {

    }

    public void disableTracking(Material mat) {

    }

    public Map<String, Integer> getMarketCapInformation() {
        return null;
    }

}