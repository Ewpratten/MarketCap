package ca.retrylife.marketcap.database;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

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
    
    // Hidden constructor to force singleton usage
    private DatabaseAPI(){}

    public void startServer() {
        
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