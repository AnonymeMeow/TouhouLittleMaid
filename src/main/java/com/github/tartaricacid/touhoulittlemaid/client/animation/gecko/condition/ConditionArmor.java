package com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.condition;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.touhoulittlemaid.util.ResourceLocationUtil.isValidResourceLocation;

public class ConditionArmor {
    private static final Pattern ID_PRE_REG = Pattern.compile("^(.+?)\\$(.*?)$");
    private static final Pattern TAG_PRE_REG = Pattern.compile("^(.+?)#(.*?)$");
    private static final String EMPTY = "";

    private final Map<EquipmentSlot, List<ResourceLocation>> idTest = Maps.newHashMap();
    private final Map<EquipmentSlot, List<TagKey<Item>>> tagTest = Maps.newHashMap();

    public void addTest(String name) {
        Matcher matcherId = ID_PRE_REG.matcher(name);
        if (matcherId.find()) {
            EquipmentSlot type = getType(matcherId.group(1));
            if (type == null) {
                return;
            }
            String id = matcherId.group(2);
            if (!isValidResourceLocation(id)) {
                return;
            }
            ResourceLocation res = ResourceLocation.parse(id);
            if (idTest.containsKey(type)) {
                idTest.get(type).add(res);
            } else {
                idTest.put(type, Lists.newArrayList(res));
            }
            return;
        }

        Matcher matcherTag = TAG_PRE_REG.matcher(name);
        if (matcherTag.find()) {
            EquipmentSlot type = getType(matcherTag.group(1));
            if (type == null) {
                return;
            }
            String id = matcherTag.group(2);
            if (!isValidResourceLocation(id)) {
                return;
            }
            TagKey<Item> tagKey = TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.parse(id)
            );
            if (tagTest.containsKey(type)) {
                tagTest.get(type).add(tagKey);
            } else {
                tagTest.put(type, Lists.newArrayList(tagKey));
            }
        }
    }

    public String doTest(IMaid maid, EquipmentSlot slot) {
        ItemStack item = maid.asEntity().getItemBySlot(slot);
        if (item.isEmpty()) {
            return EMPTY;
        }
        String result = doIdTest(maid, slot);
        if (result.isEmpty()) {
            return doTagTest(maid, slot);
        }
        return result;
    }

    private String doIdTest(IMaid maid, EquipmentSlot slot) {
        if (idTest.isEmpty()) {
            return EMPTY;
        }
        if (!idTest.containsKey(slot) || idTest.get(slot).isEmpty()) {
            return EMPTY;
        }
        List<ResourceLocation> idListTest = idTest.get(slot);
        ItemStack item = maid.asEntity().getItemBySlot(slot);
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item.getItem());
        if (registryName == null) {
            return EMPTY;
        }
        if (idListTest.contains(registryName)) {
            return slot.getName() + "$" + registryName;
        }
        return EMPTY;
    }

    private String doTagTest(IMaid maid, EquipmentSlot slot) {
        if (tagTest.isEmpty()) {
            return EMPTY;
        }
        if (!tagTest.containsKey(slot) || tagTest.get(slot).isEmpty()) {
            return EMPTY;
        }
        List<TagKey<Item>> tagListTest = tagTest.get(slot);
        ItemStack item = maid.asEntity().getItemBySlot(slot);
        return tagListTest.stream().filter(item::is).findFirst().map(itemTagKey -> slot.getName() + "#" + itemTagKey.location()).orElse(EMPTY);
    }


    @Nullable
    public static EquipmentSlot getType(String type) {
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (equipmentslot.getName().equals(type)) {
                return equipmentslot;
            }
        }
        return null;
    }
}