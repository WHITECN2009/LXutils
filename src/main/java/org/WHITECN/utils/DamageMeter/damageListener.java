package org.WHITECN.utils.DamageMeter;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.WHITECN.utils.DamageMeter.tagUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.text.DecimalFormat;
import java.util.Objects;

public class damageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            double damage = e.getDamage();
            DecimalFormat df = new DecimalFormat("#."+"0".repeat(Integer.parseInt(tagUtils.getTag(player,"DamageMeterDigits"))));
            if (!tagUtils.hasTag(player, "DamageMeterStatus") || Objects.equals(tagUtils.getTag(player, "DamageMeterStatus"), "false")){
                return;
            }
            String colorCode = getColorCode(tagUtils.getTag(player, "DamageMeterTextColor"));
            String Bold = getBoldStatus(tagUtils.getTag(player, "DamageMeterTextBold"));
            sendActionBar(player, colorCode + Bold + "本次伤害: " + df.format(damage));
        }
    }

    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public String getColorCode(String colorName) {
        switch (colorName.toLowerCase()) {
            case "black": return "§0";
            case "dark_blue": return "§1";
            case "dark_green": return "§2";
            case "dark_aqua": return "§3";
            case "dark_red": return "§4";
            case "dark_purple": return "§5";
            case "gold": return "§6";
            case "gray": return "§7";
            case "dark_gray": return "§8";
            case "blue": return "§9";
            case "green": return "§a";
            case "aqua": return "§b";
            case "red": return "§c";
            case "light_purple": return "§d";
            case "yellow": return "§e";
            case "white": return "§f";
            default: return "§f"; // 默认白色
        }
    }

    public String getBoldStatus(String Bold) {
        switch (Bold.toLowerCase()) {
            case "true": return "§l";
            case "false":
            default: return "";
        }
    }
}
