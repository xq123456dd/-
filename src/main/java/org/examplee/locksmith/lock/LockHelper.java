package org.examplee.locksmith.lock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.examplee.locksmith.registry.ModTags;

import java.util.ArrayList;
import java.util.List;

/**
 * 判断哪些方块可以上锁，以及“一次上锁”需要覆盖哪些坐标。
 * <p>
 * 兼容性说明：麦考（Macaw's）等模组的门基本都是原版 {@link DoorBlock} 的子类，
 * 所以下面用 instanceof 判断即可自动兼容；如果某个模组完全自己实现了门，
 * 只需把方块加入 {@code locksmith:lockable} 方块标签。
 */
public final class LockHelper {

    private LockHelper() {
    }

    public static boolean isLockable(BlockState state) {
        if (state.is(ModTags.Blocks.LOCKABLE)) {
            return true;
        }
        return state.getBlock() instanceof DoorBlock
                || state.getBlock() instanceof TrapDoorBlock
                || state.getBlock() instanceof FenceGateBlock;
    }

    /**
     * 计算上锁时需要一起覆盖的坐标：
     * 门 = 上下两半 + 双开门的另一半；栅栏门 = 相邻同朝向的栅栏门；其他 = 自身。
     */
    public static List<BlockPos> getLockTargets(Level level, BlockPos pos, BlockState state) {
        List<BlockPos> targets = new ArrayList<>(6);
        if (state.getBlock() instanceof DoorBlock && state.hasProperty(DoorBlock.HALF)) {
            BlockPos lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            addDoorGroup(level, lower, targets);
        } else if (state.getBlock() instanceof FenceGateBlock && state.hasProperty(FenceGateBlock.FACING)) {
            addFenceGateGroup(level, pos, state, targets);
        } else {
            add(targets, pos);
        }
        return targets;
    }

    /** 关闭这些坐标上已经打开的门 / 活板门 / 栅栏门。 */
    public static void closeOpenBlocks(Level level, List<BlockPos> targets) {
        for (BlockPos pos : targets) {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(BlockStateProperties.OPEN) && state.getValue(BlockStateProperties.OPEN)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, Boolean.FALSE), 10);
            }
        }
    }

    private static void addDoorGroup(Level level, BlockPos lower, List<BlockPos> targets) {
        add(targets, lower);
        add(targets, lower.above());

        BlockState lowerState = level.getBlockState(lower);
        if (!(lowerState.getBlock() instanceof DoorBlock)
                || !lowerState.hasProperty(DoorBlock.FACING)
                || !lowerState.hasProperty(DoorBlock.HINGE)) {
            return;
        }
        Direction facing = lowerState.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = lowerState.getValue(DoorBlock.HINGE);

        for (Direction side : sides(facing)) {
            BlockPos other = lower.relative(side);
            BlockState otherState = level.getBlockState(other);
            boolean paired = otherState.getBlock() == lowerState.getBlock()
                    && otherState.hasProperty(DoorBlock.FACING)
                    && otherState.getValue(DoorBlock.FACING) == facing
                    && otherState.hasProperty(DoorBlock.HINGE)
                    && otherState.getValue(DoorBlock.HINGE) != hinge;
            if (paired) {
                add(targets, other);
                add(targets, other.above());
            }
        }
    }

    private static void addFenceGateGroup(Level level, BlockPos pos, BlockState state, List<BlockPos> targets) {
        add(targets, pos);
        Direction facing = state.getValue(FenceGateBlock.FACING);

        for (Direction side : sides(facing)) {
            BlockPos other = pos.relative(side);
            BlockState otherState = level.getBlockState(other);
            boolean paired = otherState.getBlock() == state.getBlock()
                    && otherState.hasProperty(FenceGateBlock.FACING)
                    && otherState.getValue(FenceGateBlock.FACING) == facing;
            if (paired) {
                add(targets, other);
            }
        }
    }

    private static Direction[] sides(Direction facing) {
        return new Direction[]{facing.getClockWise(), facing.getCounterClockWise()};
    }

    private static void add(List<BlockPos> targets, BlockPos pos) {
        BlockPos immutable = pos.immutable();
        if (!targets.contains(immutable)) {
            targets.add(immutable);
        }
    }
}
