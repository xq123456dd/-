package org.examplee.locksmith.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.examplee.locksmith.Locksmith;
import org.examplee.locksmith.LocksmithConfig;
import org.examplee.locksmith.item.KeyItem;
import org.examplee.locksmith.item.LockItem;
import org.examplee.locksmith.lock.KeyData;
import org.examplee.locksmith.lock.LockData;
import org.examplee.locksmith.lock.LockHelper;
import org.examplee.locksmith.lock.LockManager;
import org.examplee.locksmith.registry.ModDataComponents;
import org.examplee.locksmith.registry.ModItems;

import java.util.List;
import java.util.UUID;

/**
 * 右键门 / 开锁 / 破坏门的处理。
 * <p>
 * 重要：所有逻辑都只在<b>服务端</b>执行，也只在服务端取消事件。
 * 如果在客户端取消 {@code RightClickBlock}，原版客户端就不会再把这个交互发给服务端了，
 * 那样上锁 / 开锁会完全失效。
 */
@EventBusSubscriber(modid = Locksmith.MOD_ID)
public final class LockEvents {

    private LockEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!LockHelper.isLockable(state)) {
            return;
        }

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        // 手持锁 -> 上锁
        if (item instanceof LockItem lockItem) {
            event.setCanceled(true);
            lock(level, pos, state, player, stack, lockItem);
            resync(level, pos, state);
            return;
        }

        // 手持钥匙 -> 绑定 / 开锁
        if (item instanceof KeyItem keyItem) {
            event.setCanceled(true);
            useKey(level, pos, state, player, stack, keyItem);
            resync(level, pos, state);
            return;
        }

        // 手里是别的东西（或者空手）而且门已经上锁 -> 不许开
        if (LockManager.isLocked(level, pos)) {
            event.setCanceled(true);
            LockData lock = LockManager.getLock(level, pos);
            if (LocksmithConfig.SHOW_LOCKED_HINT.get()) {
                player.displayClientMessage(Component
                        .translatable("msg.locksmith.locked_by", lock == null ? "?" : lock.ownerName())
                        .withStyle(ChatFormatting.RED), true);
                level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.6F, 0.8F);
            }
            resync(level, pos, state);
        }
    }

    /** 门被破坏时清理锁数据（并掉落锁）。 */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!LockHelper.isLockable(state)) {
            return;
        }

        LockManager manager = LockManager.of(level);
        if (manager == null) {
            return;
        }
        LockData lock = manager.get(pos);
        if (lock == null) {
            return;
        }

        Player player = event.getPlayer();
        boolean owner = lock.owner().equals(player.getUUID());
        if (!owner && !player.hasPermissions(2) && !LocksmithConfig.OTHERS_CAN_BREAK_LOCKED_BLOCKS.get()) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("msg.locksmith.cannot_break").withStyle(ChatFormatting.RED), true);
            return;
        }

        manager.removeByLockId(lock.lockId());
        if (LocksmithConfig.DROP_LOCK_WHEN_BROKEN.get()) {
            Block.popResource(level, pos, new ItemStack(getLockItem(lock.lockItem())));
        }
    }

    // ------------------------------------------------------------------
    // 上锁
    // ------------------------------------------------------------------

    private static void lock(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack, LockItem lockItem) {
        LockManager manager = LockManager.of(level);
        if (manager == null) {
            return;
        }

        List<BlockPos> targets = LockHelper.getLockTargets(level, pos, state);
        for (BlockPos target : targets) {
            if (manager.contains(target)) {
                player.displayClientMessage(Component.translatable("msg.locksmith.already_locked").withStyle(ChatFormatting.RED), true);
                level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.6F, 0.8F);
                return;
            }
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(lockItem);
        LockData data = new LockData(
                UUID.randomUUID(),
                player.getUUID(),
                player.getName().getString(),
                itemId == null ? "locksmith:iron_lock" : itemId.toString(),
                System.currentTimeMillis());

        for (BlockPos target : targets) {
            manager.put(target, data);
        }
        // 上锁时顺便把开着的门关上
        LockHelper.closeOpenBlocks(level, targets);

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.3F);
        player.displayClientMessage(Component.translatable("msg.locksmith.locked").withStyle(ChatFormatting.GREEN), true);
    }

    // ------------------------------------------------------------------
    // 钥匙
    // ------------------------------------------------------------------

    private static void useKey(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack, KeyItem keyItem) {
        LockManager manager = LockManager.of(level);
        if (manager == null) {
            return;
        }

        LockData lock = manager.get(pos);
        if (lock == null) {
            player.displayClientMessage(Component.translatable("msg.locksmith.not_locked").withStyle(ChatFormatting.RED), true);
            return;
        }

        // 万能钥匙 / 已经绑定到这把锁 -> 直接开锁
        if (keyItem.isMaster()) {
            unlock(level, manager, pos, lock, player);
            return;
        }

        KeyData keyData = stack.get(ModDataComponents.KEY_BINDING.get());
        if (keyData != null && keyData.lockId().equals(lock.lockId())) {
            unlock(level, manager, pos, lock, player);
            return;
        }

        boolean sneaking = player.isShiftKeyDown();
        boolean canBindNow = !LocksmithConfig.KEY_BINDING_NEEDS_SNEAK.get() || sneaking;

        if (canBindNow) {
            // 绑定 / 换绑
            if (LocksmithConfig.ONLY_OWNER_CAN_BIND_KEY.get()
                    && !lock.owner().equals(player.getUUID())
                    && !player.hasPermissions(2)) {
                player.displayClientMessage(Component.translatable("msg.locksmith.not_owner").withStyle(ChatFormatting.RED), true);
                return;
            }

            stack.set(ModDataComponents.KEY_BINDING.get(),
                    new KeyData(lock.lockId(), level.dimension().location(), pos.immutable(), lock.ownerName()));
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE);

            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.7F, 1.6F);
            player.displayClientMessage(Component.translatable("msg.locksmith.key_bound").withStyle(ChatFormatting.GREEN), true);
            return;
        }

        if (keyData == null) {
            player.displayClientMessage(Component.translatable("msg.locksmith.key_unbound").withStyle(ChatFormatting.RED), true);
        } else {
            player.displayClientMessage(Component.translatable("msg.locksmith.key_mismatch").withStyle(ChatFormatting.RED), true);
        }
        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 0.7F);
    }

    private static void unlock(Level level, LockManager manager, BlockPos pos, LockData lock, Player player) {
        manager.removeByLockId(lock.lockId());
        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.3F);
        player.displayClientMessage(Component.translatable("msg.locksmith.unlocked").withStyle(ChatFormatting.GREEN), true);

        boolean owner = lock.owner().equals(player.getUUID());
        if (owner && !player.isCreative() && LocksmithConfig.RETURN_LOCK_ON_UNLOCK.get()) {
            ItemStack returned = new ItemStack(getLockItem(lock.lockItem()));
            if (!player.getInventory().add(returned)) {
                player.drop(returned, false);
            }
        }

        // 解锁后让红石状态重新同步一次（这样被压着的拉杆可以立刻生效）
        BlockState state = level.getBlockState(pos);
        level.updateNeighborsAt(pos, state.getBlock());
    }

    // ------------------------------------------------------------------
    // 工具方法
    // ------------------------------------------------------------------

    /** 根据注册名还原成锁物品，找不到时退回铁锁。 */
    public static Item getLockItem(String lockItemId) {
        ResourceLocation id = ResourceLocation.tryParse(lockItemId);
        if (id != null) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item != Items.AIR) {
                return item;
            }
        }
        return ModItems.IRON_LOCK.get();
    }

    /**
     * 通知客户端刷新这些坐标的方块状态。
     * 客户端会预测“门被打开了”，这里把服务端真实状态推回去，避免门看起来是开的。
     */
    private static void resync(Level level, BlockPos clickedPos, BlockState clickedState) {
        List<BlockPos> targets = LockHelper.getLockTargets(level, clickedPos, clickedState);
        for (BlockPos pos : targets) {
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }
}
