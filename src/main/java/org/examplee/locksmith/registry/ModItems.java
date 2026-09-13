package org.examplee.locksmith.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.examplee.locksmith.Locksmith;
import org.examplee.locksmith.item.KeyItem;
import org.examplee.locksmith.item.LockItem;
import org.examplee.locksmith.lock.LockTier;

public final class ModItems {

    private ModItems() {
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Locksmith.MOD_ID);

    // ------- 锁（矿物锁 / 木锁 / 石锁）-------
    public static final DeferredItem<LockItem> WOOD_LOCK = lock("wood_lock", LockTier.WOOD);
    public static final DeferredItem<LockItem> STONE_LOCK = lock("stone_lock", LockTier.STONE);
    public static final DeferredItem<LockItem> IRON_LOCK = lock("iron_lock", LockTier.IRON);
    public static final DeferredItem<LockItem> GOLD_LOCK = lock("gold_lock", LockTier.GOLD);
    public static final DeferredItem<LockItem> DIAMOND_LOCK = lock("diamond_lock", LockTier.DIAMOND);
    public static final DeferredItem<LockItem> NETHERITE_LOCK = lock("netherite_lock", LockTier.NETHERITE);

    // ------- 钥匙 -------
    /** 普通钥匙，未绑定时没有作用，潜行右键已上锁的门即可绑定 */
    public static final DeferredItem<KeyItem> KEY = ITEMS.registerItem("key",
            properties -> new KeyItem(properties.stacksTo(1), false));

    /** 万能钥匙：管理员工具，可以打开任何锁（无合成配方，默认只在创造模式物品栏里） */
    public static final DeferredItem<KeyItem> MASTER_KEY = ITEMS.registerItem("master_key",
            properties -> new KeyItem(properties.stacksTo(1).rarity(Rarity.EPIC), true));

    private static DeferredItem<LockItem> lock(String name, LockTier tier) {
        return ITEMS.registerItem(name, properties -> new LockItem(properties.stacksTo(16), tier));
    }
}
