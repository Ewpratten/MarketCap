package ca.retrylife.marketcap.commands;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import ca.retrylife.marketcap.database.DatabaseAPI;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;

@CommandAlias("mcap|marketcap")
@Description("Server market cap information")
public class MarketCapCommand extends BaseCommand {

    @Subcommand("info")
    public void onInfo(CommandSender sender) {
        
        // Get the cap information
        Map<String, Integer> items = DatabaseAPI.getInstance().getMarketCapInformation();

        // Print info
        sender.sendMessage("Estimated market cap:");
        for (Entry<String, Integer> entry : items.entrySet()) {
            sender.sendMessage(String.format(" [%s]: %d", entry.getKey(), entry.getValue()));
        }

    }

    @Subcommand("addFilter")
    @Description("Start tracking a new item")
    @CommandPermission("marketcap.filter")
    public void onAdd(CommandSender sender, ItemStack item) {
        // Ensure not null
        if (item == null) {
            sender.sendMessage("Item cannot be NULL");
            return;
        }

        // Add the material to the filter
        DatabaseAPI.getInstance().enableTracking(item.getType());
        sender.sendMessage("Started tracking new item");

    }

    @Subcommand("addFilter")
    @Description("Stop tracking an item")
    @CommandPermission("marketcap.filter")
    public void onRemove(CommandSender sender, ItemStack item) {
        // Ensure not null
        if (item == null) {
            sender.sendMessage("Item cannot be NULL");
            return;
        }

        // Add the material to the filter
        DatabaseAPI.getInstance().enableTracking(item.getType());
        sender.sendMessage("Stopped tracking item");
    }

}