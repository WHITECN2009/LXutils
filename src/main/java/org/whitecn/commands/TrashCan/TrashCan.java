package org.whitecn.commands.TrashCan;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.checkerframework.checker.nullness.qual.NonNull;

import static org.whitecn.Vars.TRASHCAN_PREFIX;
import static org.whitecn.Vars.getNonPlayerWarning;

public class TrashCan implements CommandExecutor {
    String prefix = TRASHCAN_PREFIX;

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(getNonPlayerWarning(prefix));
        }
        Player player = (Player) sender;

        Inventory inventory = Bukkit.createInventory(null, 54, "这是一个垃圾桶 - LXutils");
        player.openInventory(inventory);
        return true;
    }
}
