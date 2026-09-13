package org.examplee.locksmith.lock;

/**
 * 锁的材质等级。security 越大越难被撬（预留给后续的撬锁玩法）。
 */
public enum LockTier {

    WOOD("wood", 1),
    STONE("stone", 2),
    IRON("iron", 3),
    GOLD("gold", 4),
    DIAMOND("diamond", 5),
    NETHERITE("netherite", 6);

    private final String id;
    private final int security;

    LockTier(String id, int security) {
        this.id = id;
        this.security = security;
    }

    public String getId() {
        return this.id;
    }

    public int getSecurity() {
        return this.security;
    }

    /** 语言文件里的键名，例如 lock_tier.locksmith.iron */
    public String getDescriptionId() {
        return "lock_tier.locksmith." + this.id;
    }
}
