package org.examplee.locksmith.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.examplee.locksmith.Locksmith;
import org.examplee.locksmith.lock.KeyData;

/**
 * 1.21 的数据组件：钥匙上记录“绑定到哪一把锁”。
 */
public final class ModDataComponents {

    private ModDataComponents() {
    }

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Locksmith.MOD_ID);

    /** 钥匙绑定信息（未绑定 = 没有这个组件） */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KeyData>> KEY_BINDING = DATA_COMPONENTS
            .registerComponentType("key_binding", builder -> builder
                    .persistent(KeyData.CODEC)
                    .networkSynchronized(KeyData.STREAM_CODEC)
                    .cacheEncoding());
}
