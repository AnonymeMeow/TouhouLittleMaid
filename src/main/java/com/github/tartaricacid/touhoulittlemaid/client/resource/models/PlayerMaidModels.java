package com.github.tartaricacid.touhoulittlemaid.client.resource.models;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.inner.InnerAnimation;
import com.github.tartaricacid.touhoulittlemaid.client.model.PlayerMaidModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public final class PlayerMaidModels {
    private static final Cache<String, GameProfile> GAME_PROFILE_CACHE = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static final GameProfile EMPTY_GAME_PROFILE = new GameProfile(UUID.randomUUID(), "alex");
    private static final PlayerMaidModel PLAYER_MAID_MODEL = new PlayerMaidModel(false);
    private static final PlayerMaidModel PLAYER_MAID_MODEL_SLIM = new PlayerMaidModel(true);
    private static final List<ResourceLocation> PLAYER_MAID_ANIMATION_RES = Lists.newArrayList(
            ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/default/head/default.js"),
            ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/default/head/beg.js"),
            ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/default/leg/default.js"),
            ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/player/arm/default.js"),
            ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/default/arm/swing.js"),
            ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/player/sit/default.js")
    );
    private static final ResourceLocation TEXTURE_ALEX = ResourceLocation.withDefaultNamespace("textures/entity/alex.png");
    private static final List<Object> PLAYER_MAID_ANIMATIONS = Lists.newArrayList();
    private static MaidModelInfo playerMaidInfo;
    private static ResourceLocation playerSkin;

    public static void reload() {
        PLAYER_MAID_ANIMATIONS.clear();
        for (ResourceLocation res : PLAYER_MAID_ANIMATION_RES) {
            PLAYER_MAID_ANIMATIONS.add(InnerAnimation.get(res));
        }
        playerMaidInfo = new MaidModelInfo() {
            @Override
            public ResourceLocation getTexture() {
                return playerSkin;
            }
        };
    }

    public static BedrockModel<Mob> getPlayerMaidModel(String name) {
        GameProfile newProfile = null;
        Minecraft minecraft = Minecraft.getInstance();

        try {
            newProfile = GAME_PROFILE_CACHE.get(name, () -> {
                SkullBlockEntity.fetchGameProfile(name).thenApply(gameProfile -> {
                    GameProfile profile = gameProfile.orElse(EMPTY_GAME_PROFILE);
                    GAME_PROFILE_CACHE.put(name, profile);
                    return profile;
                });
                return EMPTY_GAME_PROFILE;
            });
        } catch (ExecutionException ignore) {
        }

        if (newProfile != null) {
            PlayerSkin skin = minecraft.getSkinManager().getInsecureSkin(newProfile);
            if (skin.model() == PlayerSkin.Model.SLIM) {
                return PLAYER_MAID_MODEL_SLIM;
            }
        }
        return PLAYER_MAID_MODEL;
    }

    public static List<Object> getPlayerMaidAnimations() {
        return PLAYER_MAID_ANIMATIONS;
    }

    public static MaidModelInfo getPlayerMaidInfo(String name) {
        playerSkin = getPlayerSkin(name);
        return playerMaidInfo;
    }

    public static ResourceLocation getPlayerSkin(String name) {
        GameProfile newProfile = null;
        Minecraft minecraft = Minecraft.getInstance();

        try {
            newProfile = GAME_PROFILE_CACHE.get(name, () -> {
                SkullBlockEntity.fetchGameProfile(name).thenApply(gameProfile -> {
                    GameProfile profile = gameProfile.orElse(EMPTY_GAME_PROFILE);
                    GAME_PROFILE_CACHE.put(name, profile);
                    return profile;
                });
                return EMPTY_GAME_PROFILE;
            });
        } catch (ExecutionException ignore) {
        }

        if (newProfile != null) {
            PlayerSkin skin = minecraft.getSkinManager().getInsecureSkin(newProfile);
            return skin.texture();
        }

        return TEXTURE_ALEX;
    }
}
