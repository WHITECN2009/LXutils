package org.WHITECN.commands.CBtoFunction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class tofunctionconfirm implements CommandExecutor {
    String prefix = "§b§l[CBtoFunction_LX] §r";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + "§c§l该命令仅能由玩家执行");
            return true;
        }
        Player player = (Player) sender;
        if (args.length != 2) {
            sender.sendMessage(prefix + "§c§l用法：/tofunctionconfirm 文件名 [overwrite|append|cancel]");
            return true;
        }
        String fileName = args[0];
        String option = args[1].toLowerCase();

        // 从缓存中取出操作
        tofunction.PendingOperation op = PendingOperations.pendingMap.remove(player.getUniqueId());
        if (op == null) {
            sender.sendMessage(prefix + "§c§l没有找到待确认的操作");
            return true;
        }
        if (option.equals("cancel")) {
            sender.sendMessage(prefix + "§e已取消操作");
            return true;
        }
        String mode = option.equals("overwrite") ? "overwrite" : "append";

        File file = new File("plugins/CBtoFunction/outputFunctions", fileName + ".mcfunction");
        boolean inLine = tofunction.isAxisAlignedLine(op.x1, op.y1, op.z1, op.x2, op.y2, op.z2);
        try {
            if (inLine) {
                tofunction.writeFunction(player, op.x1, op.y1, op.z1, op.x2, op.y2, op.z2, file, mode);
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
