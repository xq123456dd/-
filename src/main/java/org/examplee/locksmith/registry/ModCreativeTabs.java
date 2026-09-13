package org.examplee.locksmith.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.examplee.locksmith.Locksmith;

public final class ModCreativeTabs {

    private ModCreativeTabs() {
    }

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Locksmith.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LOCKS = TABS.register("locksmith",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.locksmith"))
                    .icon(() -> new ItemStack(ModItems.IRON_LOCK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.WOOD_LOCK.get());
                        output.accept(ModItems.STONE_LOCK.get());
                        output.accept(ModItems.IRON_LOCK.get());
                        output.accept(ModItems.GOLD_LOCK.get());
                        output.accept(ModItems.DIAMOND_LOCK.get());
                        output.accept(ModItems.NETHERITE_LOCK.get());
                        output.accept(ModItems.KEY.get());
                        output.accept(ModItems.MASTER_KEY.get());
                    })
                    .build());
}
