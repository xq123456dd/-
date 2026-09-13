package org.examplee.locksmith.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.examplee.locksmith.LocksmithConfig;
import org.examplee.locksmith.lock.LockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让已上锁的门忽略红石信号，防止用按钮 / 拉杆 / 压力板绕过锁。
 * <p>
 * 注意：@Inject 的注入方法必须声明目标方法的<b>完整</b>参数表（后面再加上 CallbackInfo），
 * Mixin 会逐一校验描述符，少写参数会在启动时抛 InvalidInjectionException。
 * 下面这个参数表就是 Minecraft 1.21.1 的
 * {@code DoorBlock#neighborChanged(BlockState, Level, BlockPos, Block, BlockPos, boolean)}，
 * 换游戏版本时需要同步修改。
 */
@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin {

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void locksmith$ignoreRedstoneWhenLocked(BlockState state, Level level, BlockPos pos,
                                                    Block neighborBlock, BlockPos neighborPos, boolean movedByPiston,
                                                    CallbackInfo callback) {
        if (!LocksmithConfig.LOCKED_BLOCKS_IGNORE_REDSTONE.get()) {
            return;
        }
        if (LockManager.isLocked(level, pos)) {
            callback.cancel();
        }
    }
}
