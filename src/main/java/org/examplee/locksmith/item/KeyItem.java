package org.examplee.locksmith.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.examplee.locksmith.lock.KeyData;
import org.examplee.locksmith.registry.ModDataComponents;

import java.util.List;

/**
 * 钥匙物品。
 * <ul>
 *     <li>未绑定：潜行右键已上锁的门可以绑定</li>
 *     <li>已绑定：右键对应的锁即可开锁</li>
 *     <li>master = true 时为万能钥匙，可以打开任何锁</li>
 * </ul>
 */
public class KeyItem extends Item {

    private final boolean master;

    public KeyItem(Properties properties, boolean master) {
        super(properties);
        this.master = master;
    }

    public boolean isMaster() {
        return this.master;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (this.master) {
            tooltip.add(Component.translatable("tooltip.locksmith.master_key").withStyle(ChatFormatting.LIGHT_PURPLE));
            return;
        }

        KeyData data = stack.get(ModDataComponents.KEY_BINDING.get());
        if (data == null) {
            tooltip.add(Component.translatable("tooltip.locksmith.key.unbound").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.locksmith.key.bind_hint").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.locksmith.key.bound", data.ownerName()).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.locksmith.key.location",
                    data.pos().toShortString(),
                    data.dimension().toString()).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("tooltip.locksmith.key.usage").withStyle(ChatFormatting.GRAY));
        }
    }
}
