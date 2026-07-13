package org.whitecn.utils.Danmuji;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import java.util.Objects;

public class DanmuFormatter {

    /**
     * 格式化弹幕消息
     * @param danmu 弹幕对象
     * @param jsonData 原始JSON数据（用于获取粉丝牌和房管信息）
     * @return 格式化后的弹幕字符串
     */
    public static String formatDanmu(DanmuHandler.Danmu danmu, JsonObject jsonData) {
        StringBuilder formatted = new StringBuilder();

        // 处理时间戳 [颜色: aqua]
        formatted.append(ChatColor.AQUA)
                .append("[")
                .append(danmu.timeline)
                .append("] ");

        // 处理用户名前的标识
        formatted.append(formatUserBadge(jsonData));

        // 处理弹幕内容
        formatted.append(formatDanmuContent(danmu));

        return formatted.toString();
    }

    /**
     * 格式化用户徽章（房管+粉丝牌）
     * @param jsonData 原始JSON数据
     * @return 格式化后的用户标识
     */
    private static String formatUserBadge(JsonObject jsonData) {
        StringBuilder badge = new StringBuilder();

        // 设置为粗体粉色
        badge.append(ChatColor.BOLD);
        badge.append(ChatColor.of("#FFC0CB")); // 粉色

        // 检查是否为房管
        int isAdmin = jsonData.has("isadmin") ? jsonData.get("isadmin").getAsInt() : 0;
        if (isAdmin == 1) {
            badge.append(ChatColor.GRAY)
                    .append("[")
                    .append(ChatColor.RED)
                    .append("房")
                    .append(ChatColor.GRAY)
                    .append("]");
        }

        // 处理粉丝牌信息
        if (jsonData.has("medal") && jsonData.get("medal").isJsonArray()) {
            var medalArray = jsonData.get("medal").getAsJsonArray();
            if (medalArray.size() >= 3) {
                int medalLevel = medalArray.get(0).getAsInt();
                String medalName = medalArray.get(1).getAsString();

                // 等级为0不显示
                if (medalLevel > 0) {
                    badge.append(ChatColor.GRAY)
                            .append("[")
                            .append(ChatColor.of("#FFC0CB")) // 恢复粉色
                            .append(medalName)
                            .append("-")
                            .append(medalLevel)
                            .append(ChatColor.GRAY)
                            .append("]");
                }
            }
        }

        // 添加用户名和两侧的<>
        badge.append(ChatColor.GRAY)
                .append("<")
                .append(ChatColor.of("#FFC0CB")) // 用户名保持粉色
                .append(jsonData.get("nickname").getAsString())
                .append(ChatColor.GRAY)
                .append("> ");

        return badge.toString();
    }

    /**
     * 格式化弹幕内容
     * @param danmu 弹幕对象
     * @return 格式化后的弹幕内容
     */
    private static String formatDanmuContent(DanmuHandler.Danmu danmu) {
        StringBuilder content = new StringBuilder();

        // 设置字体颜色为金色
        content.append(ChatColor.GOLD);

        // 根据弹幕类型处理内容
        if (Objects.equals(danmu.danmuType, "1")) {
            // 表情弹幕
            content.append("[表情_")
                    .append(danmu.text)
                    .append("]");
        } else {
            // 普通文本弹幕
            content.append(danmu.text);
        }

        return content.toString();
    }

    /**
     * 批量格式化弹幕（用于多条弹幕）
     * @param danmus 弹幕列表
     * @param jsonDataList 对应的JSON数据列表
     * @return 格式化后的弹幕字符串（多条用换行分隔）
     */
    public static String formatMultipleDanmu(java.util.List<DanmuHandler.Danmu> danmus,
                                             java.util.List<JsonObject> jsonDataList) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < danmus.size(); i++) {
            if (i > 0) {
                result.append("\n");
            }
            result.append(formatDanmu(danmus.get(i), jsonDataList.get(i)));
        }

        return result.toString();
    }
}