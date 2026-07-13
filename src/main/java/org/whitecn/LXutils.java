package org.whitecn;

import org.whitecn.commands.CBtoFunction.ToFunction;
import org.whitecn.commands.CBtoFunction.ToFunctionConfirm;
import org.whitecn.commands.DamageMeter.DamageMeter;
import org.whitecn.commands.Danmuji.Danmuji;
import org.whitecn.commands.SizeCalculator.SizeCalculator;
import org.whitecn.commands.FakeOP.FakeOP;
import org.whitecn.utils.DamageMeter.DamageListener;
import org.whitecn.utils.Danmuji.DanmuHandler;
import org.whitecn.utils.TagUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class LXutils extends JavaPlugin {
    private static Logger logger;
    private final DanmuHandler dh = new DanmuHandler(0);

    @Override
    public void onEnable() {
        //此处注册事件
        getServer().getPluginManager().registerEvents(new DamageListener(),this);
        //此处注册命令
        Objects.requireNonNull(this.getCommand("dmgmeter")).setExecutor(new DamageMeter());
        Objects.requireNonNull(this.getCommand("tofunction")).setExecutor(new ToFunction());
        Objects.requireNonNull(this.getCommand("tofunctionconfirm")).setExecutor(new ToFunctionConfirm());
        Objects.requireNonNull(this.getCommand("sizecalc")).setExecutor(new SizeCalculator());
        Objects.requireNonNull(this.getCommand("sizecalculator")).setExecutor(new SizeCalculator());
        Objects.requireNonNull(this.getCommand("fakeop")).setExecutor(new FakeOP());
        Objects.requireNonNull(this.getCommand("dmj")).setExecutor(new Danmuji(dh));
        //此处注册Tab补全
        Objects.requireNonNull(this.getCommand("dmgmeter")).setTabCompleter(new DamageMeter());
        Objects.requireNonNull(this.getCommand("sizecalc")).setTabCompleter(new SizeCalculator());
        Objects.requireNonNull(this.getCommand("dmj")).setTabCompleter(new Danmuji(dh));
        //此处注册其他主类方法
        TagUtils.init(this);
        logger = getLogger();
        this.getLogger().info("插件已启用");

        //以下是弹幕姬循环任务 - 修改为异步执行 + 随机间隔
        new BukkitRunnable() {
            @Override
            public void run() {
                int playerIndex = 0;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (Objects.equals(TagUtils.getTag(p, "dmjStatus"), "on")) {
                        // 为每个玩家生成随机延迟 (20-60 tick，即1-3秒)，并错开执行
                        long randomDelay = 20 + (long) (Math.random() * 40) + (playerIndex * 5L);
                        playerIndex++;

                        // 使用延迟任务分散执行
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                processPlayerDanmu(p);
                            }
                        }.runTaskLater(LXutils.this, randomDelay);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 100L); // 主循环保持100tick(5秒)间隔
    }

    /**
     * 异步处理单个玩家的弹幕获取
     * @param p 玩家对象
     */
    private void processPlayerDanmu(Player p) {
        // 异步执行弹幕抓取
        CompletableFuture.supplyAsync(() -> {
            try {
                String roomID = TagUtils.getTag(p, "roomID");
                if (roomID != null && !roomID.isEmpty() && TagUtils.getTag(p,"dmjStatus").equals("on")) {
                    dh.setRoomId(Integer.parseInt(roomID));
                    return dh.fetchDanmuForPlayer(p.getName());
                }
                return "未发现新弹幕";
            } catch (Exception e) {
                getLogger().warning("处理玩家 " + p.getName() + " 的弹幕时发生错误: " + e.getMessage());
                return "未发现新弹幕";
            }
        }).thenAccept(result -> {
            // 回到主线程发送消息
            if (result != null && !result.equals("未发现新弹幕") && p.isOnline()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (p.isOnline()) {
                            p.sendMessage(result);
                        }
                    }
                }.runTask(this);
            }
        }).exceptionally(throwable -> {
            getLogger().warning("异步处理弹幕时发生异常: " + throwable.getMessage());
            return null;
        });
    }

    @Override
    public void onDisable() {
        this.getLogger().info("插件已禁用");
    }
}