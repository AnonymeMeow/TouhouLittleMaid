package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.api.game.gomoku.Point;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.MaidGomokuAI;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.getResourceLocation;

public record GomokuClientPackage(BlockPos pos, List<byte[]> chessData, Point point,
                                  int count) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GomokuClientPackage> TYPE = new CustomPacketPayload.Type<>(getResourceLocation("gomoku_to_client"));
    public static final StreamCodec<ByteBuf, List<byte[]>> BYTE_BUF_LIST_STREAM_CODEC = ByteBufCodecs.collection(
            ArrayList::new,
            ByteBufCodecs.BYTE_ARRAY,
            15
    );
    public static final StreamCodec<ByteBuf, GomokuClientPackage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            GomokuClientPackage::pos,
            BYTE_BUF_LIST_STREAM_CODEC,
            GomokuClientPackage::chessData,
            Point.POINT_STREAM_CODEC,
            GomokuClientPackage::point,
            ByteBufCodecs.VAR_INT,
            GomokuClientPackage::count,
            GomokuClientPackage::new
    );

    public GomokuClientPackage(BlockPos pos, byte[][] chessData, Point point, int count) {
        this(pos, Arrays.stream(chessData).toList(), point, count);
    }

    public static void handle(GomokuClientPackage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(GomokuClientPackage message) {
        Point aiPoint = MaidGomokuAI.getService(message.count).getPoint(message.chessData.toArray(new byte[15][]), message.point);
        int time = (int) (Math.random() * 1250) + 250;
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Minecraft.getInstance().submitAsync(() -> PacketDistributor.sendToServer(new GomokuServerPackage(message.pos, aiPoint)));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
