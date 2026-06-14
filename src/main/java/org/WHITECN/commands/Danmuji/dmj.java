package org.WHITECN.commands.Danmuji;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.WHITECN.utils.Danmuji.DanmuHandler;
import org.WHITECN.utils.TagUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class dmj implements CommandExecutor, TabCompleter {

    private final DanmuHandler dh;
    String prefix = "§b§l[Danmuji_LX]§r";

    public dmj(DanmuHandler dh) {
        this.dh = dh;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix+"§c§l该命令仅能被玩家执行");
            return true;
        }

        Player player = (Player) sender;
        TagUtils.ensureTag(player,"roomID","0");
        TagUtils.ensureTag(player,"dmjStatus","off");

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(prefix + "§c§l用法: /dmj setroomid <房间号> 或 /dmj switch");
        }
        if (Objects.equals(args[0], "setroomid")) {
            try {
                int RID = Integer.parseInt(args[1]);
                String roomTitle = DanmuHandler.getLiveTitle(RID);
                dh.setRoomId(RID);
                TagUtils.setTag(player,"roomID",String.valueOf(RID));
                TextComponent msg = new TextComponent(prefix+"§a已连接到房间：" + RID);
                msg.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(roomTitle != null ? roomTitle : "§c无法获取直播间标题")
                ));
                player.spigot().sendMessage(msg);
            } catch (NumberFormatException e) {
                sender.sendMessage(prefix+"§c§l只能输入数字哦");
            }
            return true;
        }
        if (Objects.equals(args[0], "switch")) {
            if (TagUtils.getTag(player,"dmjStatus").equals("on")) {
                TagUtils.setTag(player,"dmjStatus","off");
                sender.sendMessage(prefix+"§a弹幕监听已关闭");
            }else if (TagUtils.getTag(player,"dmjStatus").equals("off")) {
                TagUtils.setTag(player,"dmjStatus","on");
                sender.sendMessage(prefix+"§a弹幕监听已开启");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("setroomid","switch");
        }else if (args.length == 2 && args[0].equals("setroomid")) {
            return Arrays.asList("请输入数字房间全号");
        }
        return Collections.emptyList();
    }
}
