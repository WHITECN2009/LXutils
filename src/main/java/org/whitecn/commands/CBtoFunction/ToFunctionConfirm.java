package org.whitecn.commands.CBtoFunction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

import static org.whitecn.Vars.CBTOFUNCTION_PREFIX;
import static org.whitecn.Vars.getNonPlayerWarning;

public class ToFunctionConfirm implements CommandExecutor {
    String prefix = CBTOFUNCTION_PREFIX;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getNonPlayerWarning(prefix));
            return true;
        }
        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("cancel")) {
            PendingOperations.pendingMap.remove(player.getUniqueId());
            sender.sendMessage(prefix + "§e已取消操作");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(prefix + "§c§l用法：/tofunctionconfirm [overwrite|append|cancel]");
            return true;
        }
        String operation = args[0].toLowerCase();
        String fileName = PendingOperations.pendingMap.get(player.getUniqueId()).fileName;

        // 从缓存中取消操作
        ToFunction.PendingOperation pendingOperation = PendingOperations.pendingMap.remove(player.getUniqueId());
        if (pendingOperation == null) {
            sender.sendMessage(prefix + "§c§l没有找到待确认的操作");
            return true;
        }
        if (operation.equals("cancel")) {
            sender.sendMessage(prefix + "§e已取消操作");
            return true;
        }
        String mode = operation.equals("overwrite") ? "overwrite" : "append";

        File file = new File("plugins/LXutils/CBtoFunction/outputFunctions", fileName + ".mcfunction");
        boolean inLine = ToFunction.isAxisAlignedLine(pendingOperation.x1, pendingOperation.y1, pendingOperation.z1, pendingOperation.x2, pendingOperation.y2, pendingOperation.z2);
        try {
            if (inLine) {
                ToFunction.writeFunction(player, pendingOperation.x1, pendingOperation.y1, pendingOperation.z1, pendingOperation.x2, pendingOperation.y2, pendingOperation.z2, file, mode);
            }else{
                sender.sendMessage(prefix + "§c§l请传入一条直线(严重警告)");
            }
        } catch (IOException e) {
            sender.sendMessage(prefix + "§c§l文件写入失败");
            e.printStackTrace();
        }
        return true;
    }
}
