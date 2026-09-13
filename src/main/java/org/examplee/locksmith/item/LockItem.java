package org.examplee.locksmith.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.examplee.locksmith.lock.LockTier;

import java.util.List;

/**
 * 锁物品。右键门 / 活板门 / 栅栏门即可上锁（逻辑在 LockEvents 里，服务端执行）。
 */
public class LockItem extends Item {

    private final LockTier tier;

    public LockItem(Properties properties, LockTier tier) {
        super(properties);
        this.tier = tier;
    }

    public LockTier getTier() {
        return this.tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.locksmith.lock.usage").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.locksmith.lock.tier", Component.translatable(this.tier.getDescriptionId()))
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("tooltip.locksmith.lock.security", this.tier.getSecurity())
                .withStyle(ChatFormatting.DARK_GREEN));
    }
}
