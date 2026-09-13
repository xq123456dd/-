package org.examplee.locksmith;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.examplee.locksmith.registry.ModCreativeTabs;
import org.examplee.locksmith.registry.ModDataComponents;
import org.examplee.locksmith.registry.ModItems;

/**
 * 门锁 (Locksmith) 主类。
 * <p>
 * 玩法：
 * <ul>
 *     <li>手持任意锁，右键门 / 活板门 / 栅栏门 = 上锁</li>
 *     <li>手持钥匙，潜行右键已上锁的门 = 绑定钥匙</li>
 *     <li>手持绑定好的钥匙，右键对应的门 = 开锁（锁会返还给主人）</li>
 * </ul>
 * 任何模组的门只要继承原版 {@code DoorBlock}（例如麦考 Macaw's Doors）都能直接使用。
 */
@Mod(Locksmith.MOD_ID)
public class Locksmith {

    public static final String MOD_ID = "locksmith";

    public Locksmith(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, LocksmithConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
