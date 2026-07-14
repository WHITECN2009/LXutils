package org.whitecn;

public class Vars {
    public final static String CBTOFUNCTION_PREFIX = "§b§l[CBtoFunction_LX]§r ";
    public final static String DAMAGEMETER_PREFIX = "§b§l[DamageMeter_LX]§r ";
    public final static String DANMUJI_PREFIX = "§b§l[Danmuji_LX]§r ";
    public final static String SIZECALCULATOR_PREFIX = "§b§l[SizeCalculator_LX]§r ";
    public final static String MIRACLEFINDER_PREFIX = "§b§l[MiracleFinder_LX]§r ";

    public final static Integer MIRACLEFINDER_RADIUS = 5000;

    // MiracleFinder 消息文本
    public final static String MIRACLEFINDER_SCANNING = "§e正在扫描结构...";
    public final static String MIRACLEFINDER_NOTFOUND = "§c未找到任何结构";
    public final static String MIRACLEFINDER_HEADER = "§a╔════════ 结构扫描结果 ════════╗";
    public final static String MIRACLEFINDER_FOOTER = "§a╚═══════════════════════════════╝";

    // 结构名翻译 Map（使用小写 key，与 StructureType.getKey().getKey() 一致）
    public static java.util.Map<String, String> STRUCTURE_NAMES = new java.util.HashMap<String, String>() {{
        put("village", "村庄");
        put("desert_pyramid", "沙漠神殿");
        put("igloo", "雪屋");
        put("jungle_temple", "丛林神殿");
        put("swamp_hut", "女巫小屋");
        put("woodland_mansion", "林地府邸");
        put("ocean_ruin", "海洋废墟");
        put("shipwreck", "沉船");
        put("ocean_monument", "海底神殿");
        put("fortress", "下界要塞");
        put("nether_fossil", "下界化石");
        put("end_city", "末地城");
        put("bastion_remnant", "堡垒遗迹");
        put("mineshaft", "矿井");
        put("stronghold", "要塞");
        put("trial_chambers", "试验室");
        put("ruined_portal", "废弃传送门");
        put("buried_treasure", "埋藏的宝藏");
    }};

    public static String getStructureName(String key) {
        return STRUCTURE_NAMES.getOrDefault(key, key);
    }

    public static String getNonPlayerWarning(String prefix){
        return prefix + "§c§l该命令仅能被玩家执行";
    }
}