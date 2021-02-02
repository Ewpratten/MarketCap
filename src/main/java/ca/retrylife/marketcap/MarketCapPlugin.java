package ca.retrylife.marketcap;

import kr.entree.spigradle.annotations.PluginMain;

import org.bukkit.plugin.java.JavaPlugin;

import ca.retrylife.marketcap.database.DatabaseAPI;
import ca.retrylife.marketcap.events.ContainerInteraction;
import ca.retrylife.marketcap.events.InventoryInteraction;
import ca.retrylife.marketcap.events.PlayerConnection;
import io.sentry.Sentry;

@PluginMain
public class MarketCapPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Connect to sentry for debugging information
        Sentry.init(options -> {
            options.setDsn("https://326a28c0d4544344b05469612ee9a29a@o398481.ingest.sentry.io/5619922");
            options.setTracesSampleRate(0.5);
        });

        // Start the database server
        DatabaseAPI.getInstance().startServer();

        // Add event listeners
        getServer().getPluginManager().registerEvents(new PlayerConnection(), this);
        getServer().getPluginManager().registerEvents(new InventoryInteraction(), this);
        getServer().getPluginManager().registerEvents(new ContainerInteraction(), this);
    }

}