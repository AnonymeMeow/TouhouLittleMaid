package com.github.tartaricacid.touhoulittlemaid.client.resource.pojo;

import com.github.tartaricacid.touhoulittlemaid.client.resource.GeckoModelLoader;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChairModelInfo implements IModelInfo {
    private static final float RENDER_ENTITY_SCALE_MIN = 0.2f;
    private static final float RENDER_ENTITY_SCALE_MAX = 2.0f;
    private static final String GECKO_ANIMATION = ".json";

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private List<String> description;

    @SerializedName("model")
    private ResourceLocation model;

    @SerializedName("texture")
    private ResourceLocation texture;

    @SerializedName("extra_textures")
    private List<ResourceLocation> extraTextures;

    @SerializedName("model_id")
    private ResourceLocation modelId;

    @SerializedName("render_item_scale")
    private float renderItemScale = 1.0f;

    @SerializedName("render_entity_scale")
    private float renderEntityScale = 1.0f;

    @SerializedName("animation")
    private List<ResourceLocation> animation;

    @SerializedName("mounted_height")
    private float mountedYOffset;

    @SerializedName("tameable_can_ride")
    private boolean tameableCanRide = true;

    @SerializedName("no_gravity")
    private boolean noGravity = false;

    @SerializedName("is_gecko")
    private boolean isGeckoModel = false;

    @Expose(deserialize = false, serialize = false)
    private ResourceLocation cacheIconId = null;

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public List<ResourceLocation> getExtraTextures() {
        return extraTextures;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDescription() {
        return description;
    }

    @Override
    @Nullable
    public List<ResourceLocation> getAnimation() {
        return animation;
    }

    @Override
    public ResourceLocation getModelId() {
        return modelId;
    }

    @Override
    public ResourceLocation getModel() {
        return model;
    }

    @Override
    public boolean isGeckoModel() {
        return isGeckoModel;
    }

    public float getMountedYOffset() {
        return mountedYOffset;
    }

    public boolean isTameableCanRide() {
        return tameableCanRide;
    }

    @Override
    public float getRenderItemScale() {
        return renderItemScale;
    }

    public float getRenderEntityScale() {
        return renderEntityScale;
    }

    public boolean isNoGravity() {
        return noGravity;
    }

    @Override
    public ResourceLocation getCacheIconId() {
        return cacheIconId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChairModelInfo extra(ResourceLocation newModelId, ResourceLocation texture) {
        ChairModelInfo cloneInfo = new ChairModelInfo();
        cloneInfo.modelId = newModelId;
        cloneInfo.texture = texture;
        cloneInfo.cacheIconId = IModelInfo.createCacheIconId(newModelId);
        cloneInfo.name = this.name;
        cloneInfo.description = this.description;
        cloneInfo.model = this.model;
        cloneInfo.renderItemScale = this.renderItemScale;
        cloneInfo.renderEntityScale = this.renderEntityScale;
        cloneInfo.animation = this.animation;
        cloneInfo.mountedYOffset = this.mountedYOffset;
        cloneInfo.tameableCanRide = this.tameableCanRide;
        cloneInfo.noGravity = this.noGravity;
        cloneInfo.isGeckoModel = this.isGeckoModel;
        return cloneInfo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChairModelInfo decorate() {
        // description 设置为空列表
        if (description == null) {
            description = Collections.EMPTY_LIST;
        }
        // 如果 model_id 为空，抛出异常
        if (modelId == null) {
            throw new JsonSyntaxException("Expected \"model_id\" in model");
        }
        this.cacheIconId = IModelInfo.createCacheIconId(modelId);
        // 如果 model 或 texture 为空，自动生成默认位置的模型
        if (model == null) {
            model = ResourceLocation.fromNamespaceAndPath(modelId.getNamespace(), "models/entity/" + modelId.getPath() + ".json");
        }
        if (texture == null) {
            texture = ResourceLocation.fromNamespaceAndPath(modelId.getNamespace(), "textures/entity/" + modelId.getPath() + ".png");
        }
        // 如果名称为空，自动生成本地化名称
        if (name == null) {
            name = String.format("{model.%s.%s.name}", modelId.getNamespace(), modelId.getPath());
        }
        if (isGeckoModel) {
            if (animation == null || animation.isEmpty()) {
                animation = Collections.singletonList(GeckoModelLoader.DEFAULT_CHAIR_ANIMATION);
            } else {
                animation = animation.stream().filter(res -> res.getPath().endsWith(GECKO_ANIMATION)).collect(Collectors.toList());
            }
        }
        renderEntityScale = Mth.clamp(renderEntityScale, RENDER_ENTITY_SCALE_MIN, RENDER_ENTITY_SCALE_MAX);
        // 将写入的高度转换为游戏内部的高度
        mountedYOffset = (mountedYOffset - 3) * 0.0625f;
        return this;
    }
}
