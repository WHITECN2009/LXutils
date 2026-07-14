package org.whitecn.commands.MiracleFinder;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.whitecn.Vars;
import org.whitecn.utils.TagUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.whitecn.Vars.*;

public class MiracleFinder implements CommandExecutor, TabCompleter, Listener {

    private final Plugin plugin;
    private final String prefix = MIRACLEFINDER_PREFIX;
    private static final int COOLDOWN_SECONDS = 10;
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    // 结构类型 → 显示图标映射（使用小写 key，与 StructureType.getKey().getKey() 一致）
    private static final Map<String, Material> STRUCTURE_ICONS = new LinkedHashMap<>();
    static {
        STRUCTURE_ICONS.put("village", Material.EMERALD);
        STRUCTURE_ICONS.put("desert_pyramid", Material.SANDSTONE);
        STRUCTURE_ICONS.put("igloo", Material.SNOW_BLOCK);
        STRUCTURE_ICONS.put("jungle_temple", Material.MOSSY_COBBLESTONE);
        STRUCTURE_ICONS.put("swamp_hut", Material.POPPY);
        STRUCTURE_ICONS.put("woodland_mansion", Material.DARK_OAK_WOOD);
        STRUCTURE_ICONS.put("ocean_ruin", Material.PRISMARINE_SHARD);
        STRUCTURE_ICONS.put("shipwreck", Material.OAK_PLANKS);
        STRUCTURE_ICONS.put("ocean_monument", Material.PRISMARINE);
        STRUCTURE_ICONS.put("fortress", Material.NETHER_BRICK);
        STRUCTURE_ICONS.put("nether_fossil", Material.BONE_BLOCK);
        STRUCTURE_ICONS.put("end_city", Material.PURPUR_BLOCK);
        STRUCTURE_ICONS.put("bastion_remnant", Material.GILDED_BLACKSTONE);
        STRUCTURE_ICONS.put("mineshaft", Material.RAIL);
        STRUCTURE_ICONS.put("stronghold", Material.STONE_BRICKS);
        STRUCTURE_ICONS.put("trial_chambers", Material.TUFF);
        STRUCTURE_ICONS.put("ruined_portal", Material.OBSIDIAN);
        STRUCTURE_ICONS.put("buried_treasure", Material.CHEST);
    }

    // GUI 标题
    private static final String GUI_TITLE = "§8结构扫描器 - 设置";

    public MiracleFinder(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getNonPlayerWarning(prefix));
            return true;
        }

        // 如果有参数且是 "option"，打开设置界面
        if (args.length > 0 && args[0].equalsIgnoreCase("option")) {
            openStructureMenu(player);
            return true;
        }else if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(prefix + "§c用法: /mfinder [option] (不加参数以开启扫描)");
            return true;
        }

        if (!checkCooldown(player)) {
            return true;
        }

        // 否则执行扫描（使用玩家已选的结构）
        int radiusBlocks = MIRACLEFINDER_RADIUS;

        // 转换为区块数（向上取整）
        int radiusChunks = (int) Math.ceil(radiusBlocks / 16.0);

        player.sendMessage(prefix + MIRACLEFINDER_SCANNING + " §7(半径: " + radiusBlocks + " 格 ≈ " + radiusChunks + " 区块)");

        // 获取玩家选中的结构列表
        List<String> enabledStructures = getEnabledStructures(player);
        if (enabledStructures.isEmpty()) {
            player.sendMessage(prefix + "§c你还没有启用任何结构！请使用 §e/miraclefinder option §c打开设置");
            return true;
        }

        player.sendMessage(prefix + "§7已启用 " + enabledStructures.size() + " 种结构");

        scanStructures(player, radiusChunks, enabledStructures, results -> {
            if (results.isEmpty()) {
                player.sendMessage(prefix + MIRACLEFINDER_NOTFOUND);
                return;
            }

            player.sendMessage(prefix + MIRACLEFINDER_HEADER);
            World world = player.getWorld();

            results.forEach((structureKey, locations) -> {
                String structureName = Vars.getStructureName(structureKey);
                StringBuilder msg = new StringBuilder("§a║ §e" + structureName + "§7: ");

                for (int i = 0; i < locations.size(); i++) {
                    Location loc = locations.get(i);
                    double distance = player.getLocation().distance(loc);
                    if (i > 0) msg.append(" §7/ ");

                    int yDisplay = getDisplayY(world, structureKey, loc);
                    msg.append(String.format("§b%.0fm§7(§d%d§7,§d%d§7,§d%d§7)",
                            distance, loc.getBlockX(), yDisplay, loc.getBlockZ()));
                }
                msg.append(" §a║");
                player.sendMessage(msg.toString());
            });
            player.sendMessage(prefix + MIRACLEFINDER_FOOTER);
        });

        return true;
    }

    /**
     * 打开结构选择 GUI
     */
    private void openStructureMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        // 获取玩家已启用的结构
        Set<String> enabled = getEnabledStructureSet(player);

        int slot = 0;
        for (Map.Entry<String, Material> entry : STRUCTURE_ICONS.entrySet()) {
            String key = entry.getKey();
            Material material = entry.getValue();
            String displayName = Vars.getStructureName(key);
            boolean isEnabled = enabled.contains(key);

            ItemStack item = createStructureItem(material, displayName, key, isEnabled);
            inv.setItem(slot, item);
            slot++;

            // 每行 9 个，填满 54 格
            if (slot >= 54) break;
        }

        player.openInventory(inv);
    }

    /**
     * 创建结构选择物品
     */
    private ItemStack createStructureItem(Material material, String displayName, String key, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((enabled ? "§a✅ " : "§c❌ ") + "§f" + displayName);
        meta.setLore(Arrays.asList(
                "§7结构类型: §e" + key,
                "",
                (enabled ? "§a已启用" : "§c已禁用") + " §7(点击切换)"
        ));

        // 如果启用，添加附魔效果（发光）
        if (enabled) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 处理 GUI 点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        // 点击扫描按钮（最后一个格子）
        if (slot == 53) {
            player.closeInventory();
            player.performCommand("miraclefinder");
            return;
        }

        // 获取点击的物品
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasLore()) return;

        // 从 lore 中提取结构类型
        List<String> lore = meta.getLore();
        String keyLine = null;
        if (lore != null) {
            keyLine = lore.stream()
                    .filter(line -> line.contains("结构类型:"))
                    .findFirst()
                    .orElse(null);
        }

        if (keyLine == null) return;
        String key = keyLine.replace("§7结构类型: §e", "").trim();

        // 切换状态
        boolean currentState = isStructureEnabled(player, key);
        setStructureEnabled(player, key, !currentState);

        // 更新 GUI
        player.sendMessage(prefix + "§7" + Vars.getStructureName(key) +
                (currentState ? " §c已禁用" : " §a已启用"));

        // 刷新 GUI
        openStructureMenu(player);
    }

    /**
     * 获取玩家启用的结构列表
     */
    private List<String> getEnabledStructures(Player player) {
        List<String> enabled = new ArrayList<>();
        for (String key : STRUCTURE_ICONS.keySet()) {
            if (isStructureEnabled(player, key)) {
                enabled.add(key);
            }
        }
        return enabled;
    }

    /**
     * 获取玩家启用的结构集合
     */
    private Set<String> getEnabledStructureSet(Player player) {
        Set<String> enabled = new HashSet<>();
        for (String key : STRUCTURE_ICONS.keySet()) {
            if (isStructureEnabled(player, key)) {
                enabled.add(key);
            }
        }
        return enabled;
    }

    /**
     * 检查某个结构是否启用
     */
    private boolean isStructureEnabled(Player player, String structureKey) {
        String tagKey = "miraclefinder_" + structureKey.toLowerCase();
        // 如果 NBT 不存在，默认启用
        if (!TagUtils.hasTag(player, tagKey)) {
            TagUtils.setTag(player, tagKey, "true");
            return true;
        }
        return "true".equals(TagUtils.getTag(player, tagKey));
    }

    /**
     * 设置某个结构的启用状态
     */
    private void setStructureEnabled(Player player, String structureKey, boolean enabled) {
        String tagKey = "miraclefinder_" + structureKey.toLowerCase();
        TagUtils.setTag(player, tagKey, enabled ? "true" : "false");
    }

    /**
     * 获取显示用的 Y 坐标
     */
    private int getDisplayY(World world, String structureKey, Location loc) {
        int y = loc.getBlockY();

        // 地面结构 → 获取地面高度
        if (structureKey.equals("village") ||
                structureKey.equals("desert_pyramid") ||
                structureKey.equals("jungle_temple") ||
                structureKey.equals("swamp_hut") ||
                structureKey.equals("igloo") ||
                structureKey.equals("woodland_mansion")) {
            return world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
        }

        // 海底神殿 → 保持在水下
        if (structureKey.equals("ocean_monument")) {
            return y;
        }

        return y;
    }

    /**
     * 异步扫描结构
     */
    public void scanStructures(Player player, int radiusChunks, List<String> enabledStructures,
                               java.util.function.Consumer<Map<String, List<Location>>> callback) {
        World world = player.getWorld();
        Location center = player.getLocation();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, List<Location>> results = new LinkedHashMap<>();

            Registry<StructureType> registry = Registry.STRUCTURE_TYPE;

            for (StructureType type : registry) {
                NamespacedKey key = type.getKey();
                String structureKey = key.getKey();

                if (!enabledStructures.contains(structureKey)) {
                    continue;
                }

                try {
                    Location loc = Objects.requireNonNull(world.locateNearestStructure(center, type, radiusChunks, true)).getLocation();
                    results.put(structureKey, Collections.singletonList(loc));
                } catch (Exception e) {
                    // 某些结构在当前世界不存在，静默跳过
                }
            }

            if (results.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    callback.accept(Collections.emptyMap());
                    updateCooldown(player);
                });
                return;
            }

            // 按距离排序
            Map<String, List<Location>> sortedResults = results.entrySet().stream()
                    .sorted((a, b) -> {
                        double distA = center.distance(a.getValue().getFirst());
                        double distB = center.distance(b.getValue().getFirst());
                        return Double.compare(distA, distB);
                    })
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.accept(sortedResults);
                // ✅ 扫描完成后更新冷却
                updateCooldown(player);
            });
        });
    }

    private boolean checkCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        long lastUsed = cooldowns.getOrDefault(uuid, 0L);
        long currentTime = System.currentTimeMillis();
        long timeLeft = (lastUsed + COOLDOWN_SECONDS * 1000L) - currentTime;

        if (timeLeft > 0) {
            // 还在冷却中
            long secondsLeft = (timeLeft / 1000) + 1; // 向上取整
            player.sendMessage(prefix + "§c请等待 " + secondsLeft + " 秒后再使用！");
            return false;
        }

        return true;
    }

    /**
     * 更新玩家的冷却时间
     */
    private void updateCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static void cleanupCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        List<String> completions = new ArrayList<>();

        // 只有玩家可以使用，但控制台也需要能补全
        if (args.length == 1) {
            // 第一个参数：option
            List<String> options = List.of("option");
            for (String option : options) {
                if (option.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(option);
                }
            }
        }

        return completions;
    }
}