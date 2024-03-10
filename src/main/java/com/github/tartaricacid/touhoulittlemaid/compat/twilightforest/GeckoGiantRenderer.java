package com.github.tartaricacid.touhoulittlemaid.compat.twilightforest;

import javax.annotation.Nullable;

import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.GeoReplacedEntityRenderer;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import twilightforest.entity.monster.GiantMiner;

public class GeckoGiantRenderer extends GeoReplacedEntityRenderer<GeckoGiantEntity> {
    private MaidModelInfo info;
    private GeoModel geoModel;

    public GeckoGiantRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GeckoGiantModel(), new GeckoGiantEntity());
    }

    @Override
    public void render(Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (this.animatable != null && entity instanceof GiantMiner giant) {
            this.animatable.setInfo(giant, info);
        }
        ResourceLocation location = this.modelProvider.getModelLocation(animatable);
        GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(location);
        if (geoModel != null) {
            this.geoModel = geoModel;
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    public MaidModelInfo getInfo() {
        return info;
    }

    public void setInfo(MaidModelInfo info) {
        this.info = info;
    }

    @Override
    public float getHeightScale(Object entity) {
        return info.getRenderEntityScale() * 4.0f;
    }

    @Override
    public float getWidthScale(Object entity) {
        return info.getRenderEntityScale() * 4.0f;
    }

    @Override
    public int getInstanceId(Object animatable) {
        if (animatable instanceof GiantMiner giant) {
            return giant.getId();
        }
        return super.getInstanceId(animatable);
    }

    @Nullable
    @Override
    public GeoModel getGeoModel() {
        return geoModel;
    }
}