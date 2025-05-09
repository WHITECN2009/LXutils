package org.WHITECN.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class tagUtils {

    private static JavaPlugin plugin;

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static void ensureTag(Player player, String key, String defaultValue) {
        if (!hasTag(player, key)) {
            setTag(player, key, defaultValue);
        }
    }

    public static void setTag(Entity entity, String key, String value) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        entity.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
    }

    public static String getTag(Entity entity, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        return entity.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
    }

    public static void removeTag(Entity entity, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        entity.getPersistentDataContainer().remove(namespacedKey);
    }

    public static boolean hasTag(Entity entity, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        return entity.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
    }
}
