package org.WHITECN.commands.fakeop;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class fakeop implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }
        if (sender instanceof Player && !(sender.isOp())) {
            sender.sendMessage("§c未知命令。键入 \"/help\" 来获取帮助。");
        }
        String name = args[0];
        Bukkit.getOnlinePlayers().forEach(player -> {player.sendMessage("§7[" + sender.getName() + ": 已将" + name + "设为管理员]");});
        return true;
    }
}
