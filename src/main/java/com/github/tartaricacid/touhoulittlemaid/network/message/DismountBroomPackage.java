package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLoactionUtil.getResourceLocation;

public record DismountBroomPackage() implements CustomPacketPayload {
    public static final DismountBroomPackage INSTANCE = new DismountBroomPackage();

    public static final Type<DismountBroomPackage> TYPE = new Type<>(getResourceLocation("dismount_broom_package"));
    public static final StreamCodec<ByteBuf, DismountBroomPackage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(DismountBroomPackage message, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> {
                ServerPlayer sender = (ServerPlayer) context.player();
                if (sender.getRootVehicle() instanceof EntityBroom) {
                    sender.stopRiding();
                }
            });
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}