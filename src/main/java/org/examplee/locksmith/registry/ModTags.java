package org.examplee.locksmith.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.examplee.locksmith.Locksmith;

/**
 * 本模组定义的标签。
 * <p>
 * 兼容其它模组的方式：
 * <ul>
 *     <li>如果对方的“门”不是原版 DoorBlock / TrapDoorBlock / FenceGateBlock 的子类，
 *     只要把方块加进 {@code locksmith:lockable} 方块标签即可上锁。</li>
 *     <li>数据包写法：data/&lt;你的命名空间&gt;/tags/blocks/lockable.json</li>
 * </ul>
 */
public final class ModTags {

    private ModTags() {
    }

    public static final class Blocks {

        /** 可以被上锁的方块（默认包含原版的 doors / trapdoors / fence_gates 标签） */
        public static final TagKey<Block> LOCKABLE = TagKey.create(Registries.BLOCK, Locksmith.id("lockable"));

        private Blocks() {
        }
    }

    public static final class Items {

        /** 所有的锁 */
        public static final TagKey<Item> LOCKS = TagKey.create(Registries.ITEM, Locksmith.id("locks"));

        /** 所有的钥匙（含万能钥匙） */
        public static final TagKey<Item> KEYS = TagKey.create(Registries.ITEM, Locksmith.id("keys"));

        private Items() {
        }
    }
}
