package org.examplee.locksmith.lock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * 记录在钥匙（物品数据组件）上的绑定信息。
 *
 * @param lockId    所绑定锁的唯一 ID
 * @param dimension 该锁所在维度
 * @param pos       该锁所在位置（双开门 / 门的另一半会指向主方块）
 * @param ownerName 锁主人的名字（仅用于显示）
 */
public record KeyData(UUID lockId, ResourceLocation dimension, BlockPos pos, String ownerName) {

    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<KeyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUID_CODEC.fieldOf("lock_id").forGetter(KeyData::lockId),
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(KeyData::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(KeyData::pos),
            Codec.STRING.optionalFieldOf("owner", "").forGetter(KeyData::ownerName)
    ).apply(instance, KeyData::new));

    public static final StreamCodec<FriendlyByteBuf, KeyData> STREAM_CODEC = new StreamCodec<>() {

        @Override
        public KeyData decode(FriendlyByteBuf buffer) {
            UUID lockId = new UUID(buffer.readLong(), buffer.readLong());
            ResourceLocation dimension = ResourceLocation.parse(buffer.readUtf());
            BlockPos pos = buffer.readBlockPos();
            String owner = buffer.readUtf();
            return new KeyData(lockId, dimension, pos, owner);
        }

        @Override
        public void encode(FriendlyByteBuf buffer, KeyData value) {
            buffer.writeLong(value.lockId().getMostSignificantBits());
            buffer.writeLong(value.lockId().getLeastSignificantBits());
            buffer.writeUtf(value.dimension().toString());
            buffer.writeBlockPos(value.pos());
            buffer.writeUtf(value.ownerName());
        }
    };
}
