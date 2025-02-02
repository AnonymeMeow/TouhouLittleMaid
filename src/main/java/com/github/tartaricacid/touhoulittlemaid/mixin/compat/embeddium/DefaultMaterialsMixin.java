package com.github.tartaricacid.touhoulittlemaid.mixin.compat.embeddium;


import com.github.tartaricacid.touhoulittlemaid.client.renderer.sections.dynamic.DynamicChunkBuffers;
import com.github.tartaricacid.touhoulittlemaid.compat.sodium.embeddium.DynamicChunkBufferEmbeddiumCompat;
import net.minecraft.client.renderer.RenderType;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.DefaultMaterials;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Argon4W
 */
@Pseudo
@Mixin(DefaultMaterials.class)
public class DefaultMaterialsMixin {
    @Inject(method = "forRenderLayer", at = @At("HEAD"), cancellable = true)
    private static void forRenderLayer(RenderType layer, CallbackInfoReturnable<Material> cir) {
        if (DynamicChunkBuffers.DYNAMIC_CUTOUT_LAYERS.containsValue(layer)) {
            cir.setReturnValue(DynamicChunkBufferEmbeddiumCompat.DYNAMIC_CUTOUT_MATERIALS.get(layer));
            return;
        }

        if (DynamicChunkBuffers.DYNAMIC_TRANSLUCENT_LAYERS.containsValue(layer)) {
            cir.setReturnValue(DynamicChunkBufferEmbeddiumCompat.DYNAMIC_TRANSLUCENT_MATERIALS.get(layer));
        }
    }
}
