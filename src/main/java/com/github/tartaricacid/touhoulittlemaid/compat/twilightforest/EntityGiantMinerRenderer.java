package com.github.tartaricacid.touhoulittlemaid.compat.twilightforest;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import twilightforest.entity.monster.GiantMiner;

@OnlyIn(Dist.CLIENT)
public class EntityGiantMinerRenderer extends MobRenderer<GiantMiner, BedrockModel<GiantMiner>> {
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/entity/empty.png");
    private static final String DEFAULT_MODEL_ID = "geckolib:winefox";
    private MaidModelInfo info;
    private final GeckoGiantRenderer geckoGiantRenderer;

    public EntityGiantMinerRenderer(EntityRendererProvider.Context manager) {
        super(manager, new BedrockModel<>(), 1.8f);
        this.geckoGiantRenderer = new GeckoGiantRenderer(manager);
    }

    @Override
    public void render(GiantMiner giant, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        CustomPackLoader.MAID_MODELS.getInfo(DEFAULT_MODEL_ID).ifPresent(info -> this.info = info);
        
        this.geckoGiantRenderer.setInfo(this.info);
        this.geckoGiantRenderer.render(giant, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    @Override
    protected void scale(GiantMiner giant, PoseStack poseStack, float partialTickTime) {
        float scale = info.getRenderEntityScale() * 4.0f;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(GiantMiner giant) {
        if (info == null) {
            return DEFAULT_TEXTURE;
        }
        return info.getTexture();
    }

    public MaidModelInfo getInfo() {
        return info;
    }

    public EntityRenderDispatcher getDispatcher() {
        return this.entityRenderDispatcher;
    }
}
