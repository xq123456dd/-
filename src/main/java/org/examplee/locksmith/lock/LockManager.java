package org.examplee.locksmith.lock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 世界存档里的锁数据（每个维度一份）。
 * <p>
 * 之所以不用 BlockEntity：原版门没有方块实体，而且我们要兼容其它模组的门，
 * 所以用 SavedData 以坐标为索引保存。
 */
public class LockManager extends SavedData {

    private static final String DATA_NAME = "locksmith_locks";
    private static final String TAG_LOCKS = "locks";
    private static final String TAG_POS = "pos";

    private final Map<BlockPos, LockData> locks = new HashMap<>();

    /** 取得（必要时创建）某个维度的锁数据；客户端返回 null。 */
    public static LockManager of(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage()
                    .computeIfAbsent(new SavedData.Factory<>(LockManager::new, LockManager::load), DATA_NAME);
        }
        return null;
    }

    public static boolean isLocked(Level level, BlockPos pos) {
        LockManager manager = of(level);
        return manager != null && manager.contains(pos);
    }

    /** 返回该位置上的锁，没有则返回 null。 */
    public static LockData getLock(Level level, BlockPos pos) {
        LockManager manager = of(level);
        return manager == null ? null : manager.get(pos);
    }

    public boolean contains(BlockPos pos) {
        return this.locks.containsKey(pos);
    }

    public LockData get(BlockPos pos) {
        return this.locks.get(pos);
    }

    public void put(BlockPos pos, LockData data) {
        this.locks.put(pos.immutable(), data);
        this.setDirty();
    }

    /** 移除同一把锁的所有位置（门的上下半 + 双开门的另一半），返回移除数量。 */
    public int removeByLockId(UUID lockId) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos, LockData> entry : this.locks.entrySet()) {
            if (entry.getValue().lockId().equals(lockId)) {
                toRemove.add(entry.getKey());
            }
        }
        for (BlockPos pos : toRemove) {
            this.locks.remove(pos);
        }
        if (!toRemove.isEmpty()) {
            this.setDirty();
        }
        return toRemove.size();
    }

    /** 移除某个坐标上的锁（只删这一个坐标），返回被移除的锁。 */
    public LockData removeAt(BlockPos pos) {
        LockData removed = this.locks.remove(pos);
        if (removed != null) {
            this.setDirty();
        }
        return removed;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, LockData> entry : this.locks.entrySet()) {
            CompoundTag entryTag = entry.getValue().save();
            entryTag.putLong(TAG_POS, entry.getKey().asLong());
            list.add(entryTag);
        }
        tag.put(TAG_LOCKS, list);
        return tag;
    }

    private static LockManager load(CompoundTag tag, HolderLookup.Provider registries) {
        LockManager manager = new LockManager();
        ListTag list = tag.getList(TAG_LOCKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            LockData data = LockData.load(entryTag);
            if (data != null) {
                manager.locks.put(BlockPos.of(entryTag.getLong(TAG_POS)), data);
            }
        }
        return manager;
    }
}
