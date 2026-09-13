package org.examplee.locksmith;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 配置文件（common 配置，单人世界 / 服务器都生效）。
 * 具体数值改这里之后需要重新启动游戏（或者用 /reload 之后再改一次配置）。
 */
public final class LocksmithConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOCKED_BLOCKS_IGNORE_REDSTONE = BUILDER
            .comment("已上锁的门是否免疫红石信号（防止别人用按钮 / 拉杆 / 压力板绕过锁）",
                    "true = 免疫（推荐），false = 红石依然可以开关已上锁的门")
            .define("lockedBlocksIgnoreRedstone", true);

    public static final ModConfigSpec.BooleanValue OTHERS_CAN_BREAK_LOCKED_BLOCKS = BUILDER
            .comment("其他玩家是否可以破坏已上锁的门（OP / 权限等级 2 始终可以破坏）")
            .define("othersCanBreakLockedBlocks", false);

    public static final ModConfigSpec.BooleanValue DROP_LOCK_WHEN_BROKEN = BUILDER
            .comment("门被破坏时是否掉落锁")
            .define("dropLockWhenBroken", true);

    public static final ModConfigSpec.BooleanValue RETURN_LOCK_ON_UNLOCK = BUILDER
            .comment("用钥匙开锁时是否把锁返还给锁的主人")
            .define("returnLockOnUnlock", true);

    public static final ModConfigSpec.BooleanValue ONLY_OWNER_CAN_BIND_KEY = BUILDER
            .comment("是否只有锁的主人（或 OP）才能给钥匙绑定 / 重新绑定")
            .define("onlyOwnerCanBindKey", true);

    public static final ModConfigSpec.BooleanValue KEY_BINDING_NEEDS_SNEAK = BUILDER
            .comment("绑定钥匙是否需要潜行右键（true = 潜行右键绑定，普通右键开锁）")
            .define("keyBindingNeedsSneak", true);

    public static final ModConfigSpec.BooleanValue SHOW_LOCKED_HINT = BUILDER
            .comment("右键已上锁的门时是否提示“已上锁”")
            .define("showLockedHint", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private LocksmithConfig() {
    }
}
