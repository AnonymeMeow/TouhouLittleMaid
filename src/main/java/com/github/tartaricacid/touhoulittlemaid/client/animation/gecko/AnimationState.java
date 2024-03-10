package com.github.tartaricacid.touhoulittlemaid.client.animation.gecko;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.IAnimatable;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiPredicate;

public class AnimationState {
    private final String animationName;
    private final ILoopType loopType;
    private final int priority;
    private final BiPredicate<LivingEntity, AnimationEvent<IAnimatable>> predicate;

    public AnimationState(String animationName, ILoopType loopType, int priority, BiPredicate<LivingEntity, AnimationEvent<IAnimatable>> predicate) {
        this.animationName = animationName;
        this.loopType = loopType;
        this.priority = Mth.clamp(priority, Priority.HIGHEST, Priority.LOWEST);
        this.predicate = predicate;
    }

    public BiPredicate<LivingEntity, AnimationEvent<IAnimatable>> getPredicate() {
        return predicate;
    }

    public String getAnimationName() {
        return animationName;
    }

    public ILoopType getLoopType() {
        return loopType;
    }

    public int getPriority() {
        return priority;
    }
}
