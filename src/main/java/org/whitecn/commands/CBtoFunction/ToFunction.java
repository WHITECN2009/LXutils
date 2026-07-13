package org.whitecn.commands.CBtoFunction;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import static org.whitecn.Vars.CBTOFUNCTION_PREFIX;

public class ToFunction implements CommandExecutor {
    static String prefix = CBTOFUNCTION_PREFIX;

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + "§c§l该命令仅能被玩家执行");
            return true;
        }
        Player player = (Player) sender;

        if (!(player.isOp())){
            player.sendMessage(prefix + "§c§l该命令仅能被OP执行");
            return true;
        }

        if (PendingOperations.pendingMap.containsKey(player.getUniqueId())) {
            TextComponent msg = new TextComponent("§c您还有未确认的操作 如果想要取消 请点击此处 ");
            TextComponent cancel = new TextComponent("§c[取消]");
            cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tofunctionconfirm cancel"));
            msg.addExtra(cancel);
            player.spigot().sendMessage(msg);
            return true;
        }

        if (args.length != 7) {
            sender.sendMessage(prefix + "§c§l命令参数错误 用法：/tofunction x1 y1 z1 x2 y2 z2 文件名(不带后缀)");
            return true;
        }

        try {
            int x1 = Integer.parseInt(args[0]);
            int y1 = Integer.parseInt(args[1]);
            int z1 = Integer.parseInt(args[2]);
            int x2 = Integer.parseInt(args[3]);
            int y2 = Integer.parseInt(args[4]);
            int z2 = Integer.parseInt(args[5]);
            String fileName = args[6];
            boolean inLine = isAxisAlignedLine(x1, y1, z1, x2, y2, z2);

            File file = new File("plugins/LXutils/CBtoFunction/outputFunctions", fileName + ".mcfunction");
            if (!(file.getParentFile().mkdirs())){
                player.sendMessage(prefix + "§c文件创建失败");
            }

            if (file.exists()) {
                // 如果文件已存在，则先缓存操作，然后发出确认消息
                PendingOperation operation = new PendingOperation(player, x1, y1, z1, x2, y2, z2, fileName);
                PendingOperations.pendingMap.put(player.getUniqueId(), operation);

                // 创建可点击的提示信息
                TextComponent msg = new TextComponent(prefix + "§e文件已存在，选择覆盖或追加： ");
                TextComponent overwrite = new TextComponent("§a[覆盖]");
                overwrite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tofunctionconfirm " + fileName + " overwrite"));
                TextComponent append = new TextComponent("§b[追加]");
                append.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tofunctionconfirm " + fileName + " append"));
                TextComponent cancel = new TextComponent("§c[取消]");
                cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tofunctionconfirm " + fileName + " cancel"));

                msg.addExtra(" ");
                msg.addExtra(overwrite);
                msg.addExtra(" ");
                msg.addExtra(append);
                msg.addExtra(" ");
                msg.addExtra(cancel);

                player.spigot().sendMessage(msg);
                return true;
            }
            // 文件不存在时，直接写入，模式为“覆盖”
            if (inLine) {
                writeFunction(player, x1, y1, z1, x2, y2, z2, file, "overwrite");
            }else{
                sender.sendMessage(prefix + "§c§l请传入一条直线(严重警告)");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(prefix + "§c§l前6项仅支持传入整数坐标");
        } catch (IOException e) {
            sender.sendMessage(prefix + "§c§l发生文件读写异常，请联系凌星修复");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 执行命令块区域的扫描写入操作
     *
     * @param player 玩家
     * @param x1 起点x坐标
     * @param y1 起点y坐标
     * @param z1 起点z坐标
     * @param x2 终点x坐标
     * @param y2 终点y坐标
     * @param z2 终点z坐标
     * @param file 目标文件
     * @param mode "overwrite" 或 "append"
     * @throws IOException 文件写入异常
     */
    public static void writeFunction(Player player, int x1, int y1, int z1,
                                     int x2, int y2, int z2, File file, String mode) throws IOException {
        // mode 为 overwrite 时，先删除旧文件以便重写
        boolean appendFlag = mode.equalsIgnoreCase("append");
        if (mode.equalsIgnoreCase("overwrite") && file.exists()) {
            if (!(file.delete())){
                player.sendMessage(prefix + "§c文件创建失败");
            }
        }
        FileWriter fw = new FileWriter(file, appendFlag);

        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);
        int dz = Integer.compare(z2, z1);

        // 如果两个坐标完全相同，仅处理一次
        if (dx == 0 && dy == 0 && dz == 0) {
            processBlock(player, x1, y1, z1, fw);
        } else {
            // 从起点扫描到终点，沿着仅有一个轴变化的直线
            int cx = x1, cy = y1, cz = z1;
            boolean finished = false;
            while (!finished) {
                processBlock(player, cx, cy, cz, fw);
                if (cx == x2 && cy == y2 && cz == z2) {
                    finished = true;
                } else {
                    cx += dx;
                    cy += dy;
                    cz += dz;
                }
            }
        }
        fw.close();
        player.sendMessage("§b§l[CBtoFunction_LX] §a函数导出完成！ (" + file.getName() + ")");
    }

    /**
     * 检查指定坐标处的方块，如果为命令方块且命令内容非空，则写入文件
     */
    public static void processBlock(Player player, int x, int y, int z, FileWriter fw) throws IOException {
        Block block = player.getWorld().getBlockAt(x, y, z);
        Material type = block.getType();
        if (type == Material.COMMAND_BLOCK ||
                type == Material.CHAIN_COMMAND_BLOCK ||
                type == Material.REPEATING_COMMAND_BLOCK) {
            BlockState state = block.getState();
            if (state instanceof CommandBlock) {
                CommandBlock cb = (CommandBlock) state;
                String cmd = cb.getCommand();
                if (!cmd.trim().isEmpty()) {
                    fw.write(cmd + "\n");
                }
            }
        }
    }

    // 用于缓存玩家待确认操作的数据
    public static class PendingOperation {
        public Player player;
        public int x1, y1, z1, x2, y2, z2;
        public String fileName;

        public PendingOperation(Player player, int x1, int y1, int z1, int x2, int y2, int z2, String fileName) {
            this.player = player;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.fileName = fileName;
        }
    }

    public static boolean isAxisAlignedLine(int x1, int y1, int z1, int x2, int y2, int z2) {
        int diffCount = 0;
        if (x1 != x2) {
            diffCount++;
        }
        if (y1 != y2) {
            diffCount++;
        }
        if (z1 != z2) {
            diffCount++;
        }
        return diffCount == 1;
    }
}
