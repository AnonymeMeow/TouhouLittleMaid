package com.github.tartaricacid.touhoulittlemaid.compat.twilightforest;

import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationManager;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.IAnimatable;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.controller.AnimationController;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.manager.AnimationData;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.manager.AnimationFactory;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.GeckoLibUtil;

import net.minecraft.resources.ResourceLocation;
import twilightforest.entity.monster.GiantMiner;

public class GeckoGiantEntity implements IAnimatable {
    private static final ResourceLocation GECKO_DEFAULT_ID = new ResourceLocation("geckolib", "winefox");
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this, true);
    private GiantMiner giant = null;
    private MaidModelInfo info;

    @Override
    public void registerControllers(AnimationData data) {
        AnimationManager manager = AnimationManager.getInstance();
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("pre_parallel_%d_controller", i);
            String animationName = String.format("pre_parallel%d", i);
            data.addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateParallel(e, animationName)));
        }
        data.addAnimationController(new AnimationController<>(this, "main", 2, manager::predicateMain));
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            data.addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateParallel(e, animationName)));
        }
    }

    public ResourceLocation getModel() {
        return GECKO_DEFAULT_ID;
    }

    public ResourceLocation getTexture() {
        return info.getTexture();
    }

    public ResourceLocation getAnimation() {
        return GECKO_DEFAULT_ID;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public GiantMiner getGiant() {
        return this.giant;
    }

    public void setInfo(GiantMiner giant, MaidModelInfo info) {
        this.giant = giant;
        this.info = info;
    }
}