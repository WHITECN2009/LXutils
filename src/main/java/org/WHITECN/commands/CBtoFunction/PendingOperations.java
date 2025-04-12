package org.WHITECN.commands.CBtoFunction;

import org.WHITECN.commands.tofunction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PendingOperations {
    // 存储玩家的待确认操作，key 是玩家 UUID
    public static Map<UUID, tofunction.PendingOperation> pendingMap = new HashMap<>();
}
