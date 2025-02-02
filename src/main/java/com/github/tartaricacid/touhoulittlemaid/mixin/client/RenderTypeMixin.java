package com.github.tartaricacid.touhoulittlemaid.mixin.client;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.sections.RenderTypeExtension;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.sections.events.SectionGeometryRenderTypeEvents;
import com.github.tartaricacid.touhoulittlemaid.compat.sodium.SodiumCompat;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Argon4W
 */
@Mixin(RenderType.class)
public class RenderTypeMixin implements RenderTypeExtension {
    @Mutable
    @Shadow
    @Final
    public static ImmutableList<RenderType> CHUNK_BUFFER_LAYERS;
    @Shadow
    private int chunkLayerId;

    @WrapOperation(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderType;CHUNK_BUFFER_LAYERS:Lcom/google/common/collect/ImmutableList;"))
    private static void modifyChunkBufferLayers(ImmutableList<RenderType> value, Operation<Void> original) {
        SodiumCompat.init();
        if (SodiumCompat.isInstalled()) {
            original.call(value);
        } else {
            original.call(ImmutableList.builder().addAll(value).add(SectionGeometryRenderTypeEvents.getItemEntityTranslucentCull()).build());
        }
    }

    @Override
    public void eyelib$setChunkLayerId(int chunkLayerId) {
        this.chunkLayerId = chunkLayerId;
    }
}
