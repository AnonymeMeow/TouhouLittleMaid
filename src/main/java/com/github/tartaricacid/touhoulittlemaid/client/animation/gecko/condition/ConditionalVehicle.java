package com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.condition;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.List;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.isValidResourceLocation;

public class ConditionalVehicle {
    private static final String EMPTY = "";
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    private final List<TagKey<EntityType<?>>> tagTest = Lists.newArrayList();
    private final String idPre;
    private final String tagPre;

    public ConditionalVehicle() {
        this.idPre = "vehicle$";
        this.tagPre = "vehicle#";
    }

    public void addTest(String name) {
        int preSize = this.idPre.length();
        if (name.length() <= preSize) {
            return;
        }
        String substring = name.substring(preSize);
        if (name.startsWith(idPre) && isValidResourceLocation(substring)) {
            idTest.add(ResourceLocation.parse(substring));
        }
        if (name.startsWith(tagPre) && isValidResourceLocation(substring)) {
            tagTest.add(TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.parse(substring)
            ));
        }
    }

    public String doTest(Mob maid) {
        Entity vehicle = maid.getVehicle();
        if (vehicle == null || !vehicle.isAlive()) {
            return EMPTY;
        }
        String result = doIdTest(vehicle);
        if (result.isEmpty()) {
            return doTagTest(vehicle);
        }
        return result;
    }

    private String doIdTest(Entity vehicle) {
        if (idTest.isEmpty()) {
            return EMPTY;
        }
        ResourceLocation registryName = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType());
        if (registryName == null) {
            return EMPTY;
        }
        if (idTest.contains(registryName)) {
            return idPre + registryName;
        }
        return EMPTY;
    }

    private String doTagTest(Entity vehicle) {
        if (tagTest.isEmpty()) {
            return EMPTY;
        }
        return tagTest.stream().filter(tag -> vehicle.getType().is(tag)).findFirst().map(itemTagKey -> tagPre + itemTagKey.location()).orElse(EMPTY);
    }
}
