package org.whitecn.utils.DamageMeter;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.whitecn.utils.TagUtils;

import java.text.DecimalFormat;
import java.util.Objects;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        try{
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                LivingEntity victim = (LivingEntity) event.getEntity();
                double actualDamage = event.getFinalDamage();
                if (!TagUtils.hasTag(player, "DamageMeterStatus") || Objects.equals(TagUtils.getTag(player, "DamageMeterStatus"), "false")){
                    return;
                }
                double dealtDamage = event.getDamage();
                if (victim.isDead()){
                    actualDamage = Objects.requireNonNull(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
                }
                TagUtils.ensureTag(player, "DamageMeterDigits", "2");
                TagUtils.ensureTag(player, "DamageMeterTextColor", "white");
                TagUtils.ensureTag(player, "DamageMeterTextBold", "false");
                DecimalFormat df = new DecimalFormat("0." + "0".repeat(Integer.parseInt(TagUtils.getTag(player,"DamageMeterDigits"))));
                String colorCode = getColorCode(TagUtils.getTag(player, "DamageMeterTextColor"));
                String Bold = getBoldStatus(TagUtils.getTag(player, "DamageMeterTextBold"));
                sendActionBar(player, colorCode + Bold + "理论伤害: " + df.format(dealtDamage) + "§7|§r" + colorCode + Bold + "实际伤害: " + df.format(actualDamage));
            }
        }catch (Exception ignored){}
    }

    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public String getColorCode(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "black" -> "§0";
            case "dark_blue" -> "§1";
            case "dark_green" -> "§2";
            case "dark_aqua" -> "§3";
            case "dark_red" -> "§4";
            case "dark_purple" -> "§5";
            case "gold" -> "§6";
            case "gray" -> "§7";
            case "dark_gray" -> "§8";
            case "blue" -> "§9";
            case "green" -> "§a";
            case "aqua" -> "§b";
            case "red" -> "§c";
            case "light_purple" -> "§d";
            case "yellow" -> "§e";
            case "white" -> "§f";
            default -> "§f"; // 默认白色
        };
    }

    public String getBoldStatus(String Bold) {
        if (Bold.equalsIgnoreCase("true")) {
            return "§l";
        }else {
            return "";
        }
    }
}
