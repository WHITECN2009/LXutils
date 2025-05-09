package org.WHITECN.commands.SizeCalculator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.DecimalFormat;

public class sizecalc implements CommandExecutor {
    String prefix = "§b§l[SizeCalculator_LX] §r";
    int totalBlocks;
    double bigChest,chest;
    DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(args.length == 1) && !(args.length == 6)) {
            sender.sendMessage(prefix+"§c§l用法: /sizecalc <x1> <y1> <z1> <x2> <y2> <z2> 或 /sizecalc <方块总数>");
            return true;
        }
        if (args.length == 1) {
            try{
                totalBlocks = Integer.parseInt(args[0]);
                chest = totalBlocks / 1728.0;
                bigChest = totalBlocks / 3456.0;
                if (totalBlocks < 0) {
                    sender.sendMessage("§c§l不要传入负数啦！");
                    return true;
                }
                if (totalBlocks <= 64){
                    sender.sendMessage("§c§l\n这个数量...认真的吗...");
                    return true;
                }
                sender.sendMessage("§b§l\n分解结果如下:§r\n§7 - 方块数量为: §r§6§l"+totalBlocks/64+" §r§7组 + §r§6§l"+totalBlocks%64+"§r§7个 (共 "+totalBlocks+"§7 个)\n§7相当于:\n -  §r§6§l"+decimalFormat.format(bigChest)+" §r§7个大箱子\n  和\n - §r§6§l"+decimalFormat.format(chest)+" §r§7个潜影盒/小箱子\n");
                return true;
            }catch(NumberFormatException e){
                sender.sendMessage(prefix+"§c§l该处只能传入整数！");
                return true;
            }
        }else{
            try{
                int x1 = Integer.parseInt(args[0]);
                int y1 = Integer.parseInt(args[1]);
                int z1 = Integer.parseInt(args[2]);
                int x2 = Integer.parseInt(args[3]);
                int y2 = Integer.parseInt(args[4]);
                int z2 = Integer.parseInt(args[5]);
                totalBlocks = Math.abs(x1-x2)*Math.abs(y1-y2)*Math.abs(z1-z2);
                chest = totalBlocks / 1728.0;
                bigChest = totalBlocks / 3456.0;
                sender.sendMessage("§b§l\n分解结果如下:§r\n§7 - 方块数量为: §r§6§l"+totalBlocks/64+" §r§7组 + §r§6§l"+totalBlocks%64+"§r§7个 (共 "+totalBlocks+"§7 个)\n§7相当于:\n -  §r§6§l"+decimalFormat.format(bigChest)+" §r§7个大箱子\n  和\n - §r§6§l"+decimalFormat.format(chest)+" §r§7个潜影盒/小箱子\n");
                return true;
            }catch(NumberFormatException e){
                sender.sendMessage(prefix+"§c§l请只传入整数坐标！");
                return true;
            }
        }
    }
}
