package ca.retrylife.marketcap.commands;

import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ca.retrylife.marketcap.database.DatabaseAPI;
import ca.retrylife.marketcap.events.utils.ChunkySearcher;
import ca.retrylife.marketcap.util.SentryUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import io.sentry.Sentry;

@CommandAlias("mcap|marketcap")
@Description("Server market cap information")
public class MarketCapCommand extends BaseCommand {

    @Subcommand("info")
    public void onInfo(CommandSender sender) {
        SentryUtil.breadcrumb(getClass(), "Command run: /mcap info");

        // Get the cap information
        Map<String, Integer> items;
        try {
            items = DatabaseAPI.getInstance().getMarketCapInformation();
        } catch (BackingStoreException e) {
            Sentry.captureException(e);
            e.printStackTrace();

            // Notify user
            sender.sendMessage(
                    "An internal plugin error occurred. Contact the server admin (more information in server logs)");
            return;
        }

        // Print info
        sender.sendMessage("Estimated market cap:");
        for (Entry<String, Integer> entry : items.entrySet()) {
            sender.sendMessage(String.format(" [%s]: %d", entry.getKey(), entry.getValue()));
        }

    }

    @Subcommand("dbInfo")
    @Description("Perform a database dump")
    @CommandPermission("marketcap.debug")
    public void onDbInfo(CommandSender sender) {
        SentryUtil.breadcrumb(getClass(), "Command run: /mcap dbInfo");

        // Get the db information
        Map<String, String> databaseDump;
        try {
            databaseDump = DatabaseAPI.getInstance().dumpDB();
        } catch (BackingStoreException e) {
            Sentry.captureException(e);
            e.printStackTrace();

            // Notify user
            sender.sendMessage(
                    "An internal plugin error occurred. Contact the server admin (more information in server logs)");
            return;
        }

        // Print info
        sender.sendMessage("Database:");
        for (Entry<String, String> entry : databaseDump.entrySet()) {
            sender.sendMessage(String.format(" %s = %s", entry.getKey(), entry.getValue()));
        }

    }
    
    @Subcommand("reload")
    @Description("Force-search an area for containers")
    @CommandPermission("marketcap.debug")
    public void onForceSearch(CommandSender sender, int radius) {
        SentryUtil.breadcrumb(getClass(), "Command run: /mcap reload");

        // Ensure the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Non-players cannot execute this command");
            return;
        }

        // Get the player location
        Location location = ((Player) sender).getLocation();
       
        // Perform search
        sender.sendMessage("Searching... expect server lag");
        ChunkySearcher searcher = new ChunkySearcher(location);
        searcher.search(sender, radius);
        sender.sendMessage("Done search");
        
    }
    

    @Subcommand("addFilter")
    @Description("Start tracking a new item")
    @CommandPermission("marketcap.filter")
    public void onAdd(CommandSender sender, Material mat) {
        SentryUtil.breadcrumb(getClass(), "Command run: /mcap addFilter");

        // Ensure not null
        if (mat == null) {
            sender.sendMessage("Item cannot be NULL");
            return;
        }

        // Add the material to the filter
        DatabaseAPI.getInstance().enableTracking(mat, sender);
        sender.sendMessage("Started tracking new item");

    }

    @Subcommand("removeFilter")
    @Description("Stop tracking an item")
    @CommandPermission("marketcap.filter")
    public void onRemove(CommandSender sender, Material mat) {
        SentryUtil.breadcrumb(getClass(), "Command run: /mcap removeFilter");

        // Ensure not null
        if (mat == null) {
            sender.sendMessage("Item cannot be NULL");
            return;
        }

        // Add the material to the filter
        DatabaseAPI.getInstance().enableTracking(mat, sender);
        sender.sendMessage("Stopped tracking item");
    }

}