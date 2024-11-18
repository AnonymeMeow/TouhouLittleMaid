package com.github.tartaricacid.touhoulittlemaid.compat.sodium;

import com.github.tartaricacid.touhoulittlemaid.compat.sodium.embeddium.DynamicChunkBufferEmbeddiumCompat;
import com.github.tartaricacid.touhoulittlemaid.compat.sodium.sodium.DynamicChunkBufferSodiumCompat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class SodiumCompat {
    private static final String SODIUM_ID = "sodium";
    private static final String EMBEDDIUM_ID = "embeddium";

    private static boolean IS_INIT = false;
    private static boolean SODIUM_INSTALLED;
    private static boolean EMBEDDIUM_INSTALLED;

    public static void init() {
        if (!IS_INIT) {
            SODIUM_INSTALLED = LoadingModList.get().getModFileById(SODIUM_ID) != null;
            EMBEDDIUM_INSTALLED = LoadingModList.get().getModFileById(EMBEDDIUM_ID) != null;
            IS_INIT = true;
        }
    }

    public static RenderLevelStageEvent.Stage getCutoutRenderStage() {
        return isInstalled() ? RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS : RenderLevelStageEvent.Stage.AFTER_PARTICLES;
    }

    public static boolean isInstalled() {
        return SODIUM_INSTALLED || EMBEDDIUM_INSTALLED;
    }

    public static RenderType addSodiumCutoutPass(ResourceLocation resourceLocation, RenderType cutoutRenderType) {
        return Util.make(cutoutRenderType, renderType -> {
            if (SODIUM_INSTALLED) {
                DynamicChunkBufferSodiumCompat.markCutout(renderType, resourceLocation);
            } else if (EMBEDDIUM_INSTALLED) {
                DynamicChunkBufferEmbeddiumCompat.markCutout(renderType, resourceLocation);
            }
        });
    }

    public static RenderType addSodiumTranslucentPass(ResourceLocation resourceLocation, RenderType translucentRenderType) {
        return Util.make(translucentRenderType, renderType -> {
            if (SODIUM_INSTALLED) {
                DynamicChunkBufferSodiumCompat.markTranslucent(renderType, resourceLocation);
            } else if (EMBEDDIUM_INSTALLED) {
                DynamicChunkBufferEmbeddiumCompat.markTranslucent(renderType, resourceLocation);
            }
        });
    }
}
