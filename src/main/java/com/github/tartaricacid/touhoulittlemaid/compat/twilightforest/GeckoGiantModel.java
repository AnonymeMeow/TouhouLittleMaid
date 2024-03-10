package com.github.tartaricacid.touhoulittlemaid.compat.twilightforest;

import java.util.List;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.IAnimatable;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.MolangParser;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.processor.IBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.model.AnimatedGeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.model.provider.data.EntityModelData;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.MolangUtils;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.monster.GiantMiner;

public class GeckoGiantModel extends AnimatedGeoModel {
    private static final ResourceLocation GECKO_DEFAULT_ID = new ResourceLocation("geckolib", "winefox");
    private static final ResourceLocation GECKO_DEFAULT_TEXTURE = new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/entity/empty.png");

    @Override
    public ResourceLocation getModelLocation(Object object) {
        return GECKO_DEFAULT_ID;
    }

    @Override
    public ResourceLocation getTextureLocation(Object object) {
        if (object instanceof GeckoGiantEntity geckoGiant) {
            return geckoGiant.getTexture();
        }
        return GECKO_DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Object object) {
        return GECKO_DEFAULT_ID;
    }

    @Override
    public void setMolangQueries(IAnimatable animatable, double seekTime) {
    }

    @Override
    public void setCustomAnimations(IAnimatable animatable, int instanceId, AnimationEvent animationEvent) {
        List extraData = animationEvent.getExtraData();
        MolangParser parser = GeckoLibCache.getInstance().parser;
        if (!Minecraft.getInstance().isPaused() && extraData.size() == 1 && extraData.get(0) instanceof EntityModelData data
                && animatable instanceof GeckoGiantEntity geckoGiant && geckoGiant.getGiant() != null) {
            GiantMiner giantMiner = geckoGiant.getGiant();
            setParserValue(animationEvent, parser, data, giantMiner);
            super.setCustomAnimations(animatable, instanceId, animationEvent);
            IBone head = getCurrentModel().head;
            if (head != null) {
                head.setRotationX(head.getRotationX() + (float) Math.toRadians(data.headPitch));
                head.setRotationY(head.getRotationY() + (float) Math.toRadians(data.netHeadYaw));
            }
        } else {
            super.setCustomAnimations(animatable, instanceId, animationEvent);
        }
    }

    private static void setParserValue(AnimationEvent<GeckoGiantEntity> animationEvent, MolangParser parser, EntityModelData data, GiantMiner giant) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        parser.setValue("query.actor_count", () -> mc.level.getEntityCount());
        parser.setValue("query.body_x_rotation", giant::getXRot);
        parser.setValue("query.body_y_rotation", () -> Mth.wrapDegrees(giant.getYRot()));
        parser.setValue("query.cardinal_facing_2d", () -> giant.getDirection().get3DDataValue());
        parser.setValue("query.distance_from_camera", () -> mc.gameRenderer.getMainCamera().getPosition().distanceTo(giant.position()));
        parser.setValue("query.eye_target_x_rotation", () -> giant.getViewXRot(0));
        parser.setValue("query.eye_target_y_rotation", () -> giant.getViewYRot(0));
        parser.setValue("query.ground_speed", () -> {
            Vec3 velocity = giant.getDeltaMovement();
            return 20 * Mth.sqrt((float) ((velocity.x * velocity.x) + (velocity.z * velocity.z)));
        });

        parser.setValue("query.has_rider", () -> MolangUtils.booleanToFloat(giant.isVehicle()));
        parser.setValue("query.head_x_rotation", () -> data.netHeadYaw);
        parser.setValue("query.head_y_rotation", () -> data.headPitch);
        parser.setValue("query.health", giant::getHealth);
        parser.setValue("query.hurt_time", () -> giant.hurtTime);

        parser.setValue("query.is_eating", () -> MolangUtils.booleanToFloat(giant.getUseItem().getUseAnimation() == UseAnim.EAT));
        parser.setValue("query.is_first_person", () -> MolangUtils.booleanToFloat(mc.options.getCameraType() == CameraType.FIRST_PERSON));
        parser.setValue("query.is_in_water", () -> MolangUtils.booleanToFloat(giant.isInWater()));
        parser.setValue("query.is_in_water_or_rain", () -> MolangUtils.booleanToFloat(giant.isInWaterRainOrBubble()));
        parser.setValue("query.is_jumping", () -> MolangUtils.booleanToFloat(!giant.isPassenger() && !giant.onGround() && !giant.isInWater()));
        parser.setValue("query.is_on_fire", () -> MolangUtils.booleanToFloat(giant.isOnFire()));
        parser.setValue("query.is_on_ground", () -> MolangUtils.booleanToFloat(giant.onGround()));
        parser.setValue("query.is_playing_dead", () -> MolangUtils.booleanToFloat(giant.isDeadOrDying()));
        parser.setValue("query.is_riding", () -> MolangUtils.booleanToFloat(giant.isPassenger()));
        parser.setValue("query.is_sleeping", () -> MolangUtils.booleanToFloat(giant.isSleeping()));
        parser.setValue("query.is_sneaking", () -> MolangUtils.booleanToFloat(giant.onGround() && giant.getPose() == Pose.CROUCHING));
        parser.setValue("query.is_spectator", () -> MolangUtils.booleanToFloat(giant.isSpectator()));
        parser.setValue("query.is_sprinting", () -> MolangUtils.booleanToFloat(giant.isSprinting()));
        parser.setValue("query.is_swimming", () -> MolangUtils.booleanToFloat(giant.isSwimming()));
        parser.setValue("query.is_using_item", () -> MolangUtils.booleanToFloat(giant.isUsingItem()));
        parser.setValue("query.item_in_use_duration", () -> giant.getTicksUsingItem() / 20.0);
        parser.setValue("query.item_remaining_use_duration", () -> giant.getUseItemRemainingTicks() / 20.0);

        parser.setValue("query.max_health", giant::getMaxHealth);
        parser.setValue("query.modified_distance_moved", () -> giant.walkDist);
        parser.setValue("query.moon_phase", () -> mc.level.getMoonPhase());

        parser.setValue("query.time_of_day", () -> MolangUtils.normalizeTime(mc.level.getDayTime()));
        parser.setValue("query.time_stamp", () -> mc.level.getDayTime());
        parser.setValue("query.vertical_speed", () -> 20 * (float) (giant.position().y - giant.yo));
        parser.setValue("query.walk_distance", () -> giant.moveDist);
        parser.setValue("query.yaw_speed", () -> {
            double seekTime = animationEvent.getAnimationTick();
            return giant.getViewYRot((float) seekTime - giant.getViewYRot((float) seekTime - 0.1f));
        });

        parser.setValue("ysm.head_yaw", () -> data.netHeadYaw);
        parser.setValue("ysm.head_pitch", () -> data.headPitch);

        parser.setValue("ysm.is_passenger", () -> MolangUtils.booleanToFloat(giant.isPassenger()));
        parser.setValue("ysm.is_sleep", () -> MolangUtils.booleanToFloat(giant.getPose() == Pose.SLEEPING));
        parser.setValue("ysm.is_sneak", () -> MolangUtils.booleanToFloat(giant.onGround() && giant.getPose() == Pose.CROUCHING));
        parser.setValue("ysm.is_riptide", () -> MolangUtils.booleanToFloat(giant.isAutoSpinAttack()));

        parser.setValue("ysm.armor_value", giant::getArmorValue);
        parser.setValue("ysm.hurt_time", () -> giant.hurtTime);
    }
}