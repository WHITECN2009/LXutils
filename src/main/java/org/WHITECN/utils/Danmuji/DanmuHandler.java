package org.WHITECN.utils.Danmuji;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.*;
import com.google.gson.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;

public class DanmuHandler {

    private static final String API =
            "https://api.live.bilibili.com/xlive/web-room/v1/dM/gethistory";

    private int roomId;
    private final HttpClient client = HttpClient.newHttpClient();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 一级缓存：全局弹幕数据（按房间号）
    private String lastTimestamp = "1970-01-01 00:00:00";
    private Map<String, Danmu> globalDanmuMap = new LinkedHashMap<>();

    // 二级缓存：每个玩家的独立去重缓存
    // Key: 玩家名称, Value: 该玩家已接收的弹幕ID集合
    private Map<String, Set<String>> playerDanmuCache = new HashMap<>();

    // 玩家最后弹幕时间缓存
    private Map<String, String> playerLastTimestamp = new HashMap<>();

    // 弹幕数据结构
    public static class Danmu {
        String text;
        String nickname;
        String timeline;
        String danmuType;

        @Deprecated
        @Override
        public String toString() {
            return "[请使用 DanmuFormatter.formatDanmu() 方法]";
        }

    }

    // 构造方法：初始化时传入房间号
    public DanmuHandler(int roomId) {
        this.roomId = roomId;
    }

    // 写入房间号
    public void setRoomId(int roomId) {
        this.roomId = roomId;
        this.lastTimestamp = "1970-01-01 00:00:00"; // 切换房间后清空全局缓存
        // 不清空玩家二级缓存，因为玩家可能切换房间
    }

    /**
     * 为指定玩家获取新弹幕（使用二级缓存去重）
     * @param playerName 玩家名称
     * @return 新弹幕字符串
     */
    public String fetchDanmuForPlayer(String playerName) {
        if (roomId == 0) {
            return "未发现新弹幕";
        }

        try {
            String url = API + "?roomid=" + roomId + "&room_type=0";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");
            JsonArray room = data.getAsJsonArray("room");

            // 初始化玩家缓存（如果不存在）
            initializePlayerCache(playerName);

            // 获取玩家特定的缓存
            Set<String> playerCache = playerDanmuCache.get(playerName);
            String playerLastTime = playerLastTimestamp.get(playerName);
            LocalDateTime playerLastDateTime = LocalDateTime.parse(playerLastTime, formatter);

            Map<String, Danmu> tempGlobalMap = new LinkedHashMap<>();
            StringBuilder sb = new StringBuilder();
            LocalDateTime newestGlobalTime = LocalDateTime.parse(lastTimestamp, formatter);
            LocalDateTime newestPlayerTime = playerLastDateTime;

            int counter = 1;
            for (JsonElement e : room) {
                JsonObject obj = e.getAsJsonObject();

                Danmu d = new Danmu();
                d.text = obj.get("text").getAsString();
                d.nickname = obj.get("nickname").getAsString();
                d.timeline = obj.get("timeline").getAsString();
                d.danmuType = obj.get("dm_type").getAsString();

                String id = obj.get("id_str").getAsString();
                tempGlobalMap.put(id, d);

                LocalDateTime dmTime = LocalDateTime.parse(d.timeline, formatter);

                // 全局时间过滤 + 玩家缓存去重
                if (dmTime.isAfter(playerLastDateTime) && !playerCache.contains(id)) {
                    String formattedDanmu = DanmuFormatter.formatDanmu(d, obj);
                    sb.append(formattedDanmu).append("\n");

                    // 添加到玩家缓存
                    playerCache.add(id);

                    // 更新玩家最新时间
                    if (dmTime.isAfter(newestPlayerTime)) {
                        newestPlayerTime = dmTime;
                    }
                }

                // 更新全局最新时间
                if (dmTime.isAfter(newestGlobalTime)) {
                    newestGlobalTime = dmTime;
                }
            }

            // 更新存储
            this.globalDanmuMap = tempGlobalMap;
            this.lastTimestamp = newestGlobalTime.format(formatter);
            this.playerLastTimestamp.put(playerName, newestPlayerTime.format(formatter));

            // 清理过期缓存（可选：防止缓存过大）
            cleanupPlayerCache(playerName);

            String result = sb.toString().trim();
            return result.isEmpty() ? "未发现新弹幕" : result;

        } catch (Exception e) {
            e.printStackTrace();
            return "未发现新弹幕";
        }
    }

    /**
     * 初始化玩家缓存
     */
    private void initializePlayerCache(String playerName) {
        if (!playerDanmuCache.containsKey(playerName)) {
            playerDanmuCache.put(playerName, new HashSet<>());
        }
        if (!playerLastTimestamp.containsKey(playerName)) {
            playerLastTimestamp.put(playerName, "1970-01-01 00:00:00");
        }
    }

    /**
     * 清理玩家缓存（防止内存泄漏）
     */
    private void cleanupPlayerCache(String playerName) {
        Set<String> cache = playerDanmuCache.get(playerName);
        if (cache != null && cache.size() > 1000) {
            // 当缓存超过1000条时，清理一半最旧的记录
            // 这里简单实现：清空缓存，实际可以根据时间戳清理
            cache.clear();
        }
    }

    /**
     * 移除玩家缓存（当玩家关闭弹幕姬时调用）
     */
    public void removePlayerCache(String playerName) {
        playerDanmuCache.remove(playerName);
        playerLastTimestamp.remove(playerName);
    }

    public static String getLiveTitle(int roomId) {
        try {
            String apiUrl = "https://api.live.bilibili.com/room/v1/Room/get_info?room_id=" + roomId;
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                JSONObject json = new JSONObject(reader.readLine());
                return json.getJSONObject("data").getString("title");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取所有玩家的缓存信息（用于调试）
     */
    public Map<String, Integer> getPlayerCacheInfo() {
        Map<String, Integer> info = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : playerDanmuCache.entrySet()) {
            info.put(entry.getKey(), entry.getValue().size());
        }
        return info;
    }

    // 获取字典形式（全量）
    public Map<String, Danmu> getDanmuMap() {
        return globalDanmuMap;
    }
}