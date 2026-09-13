package org.examplee.locksmith.lock;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * 一把锁的存档数据。同一扇门的两个方块（上下半门、双开门）共用同一份实例。
 *
 * @param lockId    唯一 ID（钥匙靠它判断能不能开）
 * @param owner     锁主人的 UUID
 * @param ownerName 锁主人的名字（用于提示）
 * @param lockItem  锁物品的注册名（破坏 / 开锁时掉落对应的锁）
 * @param lockedAt  上锁时间戳（毫秒）
 */
public record LockData(UUID lockId, UUID owner, String ownerName, String lockItem, long lockedAt) {

    private static final String TAG_LOCK_ID = "lock_id";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_OWNER_NAME = "owner_name";
    private static final String TAG_LOCK_ITEM = "lock_item";
    private static final String TAG_LOCKED_AT = "locked_at";

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_LOCK_ID, this.lockId.toString());
        tag.putString(TAG_OWNER, this.owner.toString());
        tag.putString(TAG_OWNER_NAME, this.ownerName);
        tag.putString(TAG_LOCK_ITEM, this.lockItem);
        tag.putLong(TAG_LOCKED_AT, this.lockedAt);
        return tag;
    }

    /** 读取存档数据，数据损坏时返回 null。 */
    public static LockData load(CompoundTag tag) {
        try {
            UUID lockId = UUID.fromString(tag.getString(TAG_LOCK_ID));
            UUID owner = UUID.fromString(tag.getString(TAG_OWNER));
            return new LockData(lockId, owner, tag.getString(TAG_OWNER_NAME), tag.getString(TAG_LOCK_ITEM), tag.getLong(TAG_LOCKED_AT));
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
