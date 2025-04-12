package org.WHITECN.commands;

import org.WHITECN.utils.tagUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class dmgmeter implements CommandExecutor, TabCompleter {
    String prefix = "§b§l[DamageMeter]§r ";

    List<String> colors = Arrays.asList("white", "red", "blue", "green", "yellow", "aqua", "gold", "gray", "dark_red", "dark_green", "dark_blue");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + "§c§l该命令仅能被玩家执行");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(prefix + "§c§l用法: /dmgmeter [switch|color|digits]");
            return true;
        }

        // 初始化默认标签
        tagUtils.ensureTag(player, "DamageMeterDigits", "2");
        tagUtils.ensureTag(player, "DamageMeterTextColor", "white");
        tagUtils.ensureTag(player, "DamageMeterTextBold", "false");

        String option = args[0].toLowerCase();

        switch (option) {
            case "switch":
                String currentSwitch = tagUtils.getTag(player, "DamageMeterStatus");
                boolean newSwitchState = currentSwitch == null || currentSwitch.equalsIgnoreCase("false");
                tagUtils.setTag(player, "DamageMeterStatus", newSwitchState ? "true" : "false");
                player.sendMessage(prefix + "§b§l切换成功 当前状态：" + (newSwitchState ? "开" : "关"));
                break;

            case "bold":
                String currentBold = tagUtils.getTag(player, "DamageMeterTextBold");
                boolean newBoldState = currentBold == null || currentBold.equalsIgnoreCase("false");
                tagUtils.setTag(player, "DamageMeterTextBold", newBoldState ? "true" : "false");
                player.sendMessage(prefix + "§b§l切换成功 当前状态：" + (newBoldState ? "粗体" : "正常"));
                break;

            case "color":
                if (args.length < 2) {
                    player.sendMessage(prefix + "§c§l用法: /dmgmeter color <颜色>");
                    return true;
                }
                String color = args[1].toLowerCase();
                if (!colors.contains(color)) {
                    player.sendMessage(prefix + "§c§l无效颜色，可选: " + String.join(", ", colors));
                    return true;
                }
                tagUtils.setTag(player, "DamageMeterTextColor", color);
                player.sendMessage(prefix + "§a已设置颜色为: " + color);
                break;

            case "digits":
                if (args.length < 2) {
                    player.sendMessage(prefix + "§c§l用法: /dmgmeter digits <1~5>");
                    return true;
                }
                try {
                    int digits = Integer.parseInt(args[1]);
                    if (digits < 1 || digits > 5) {
                        throw new NumberFormatException();
                    }
                    tagUtils.setTag(player, "DamageMeterDigits", String.valueOf(digits));
                    player.sendMessage(prefix + "§a小数保留位已设置为: " + digits);
                } catch (NumberFormatException e) {
                    player.sendMessage(prefix + "§c请输入 1~5 之间的数字");
                }
                break;

            default:
                player.sendMessage(prefix + "§c§l未知选项: " + option);
                break;
        }

        return true;
    }

    // Tab 补全
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("switch", "color", "digits");
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "color":
                    return colors;
                case "digits":
                    return Arrays.asList("1", "2", "3", "4", "5");
            }
        }
        return Collections.emptyList();
    }
}
