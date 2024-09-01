package com.github.tartaricacid.touhoulittlemaid.init;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class InitKeyBindings {
    private static final String KEY_PREFIX = "key." + TouhouLittleMaid.MOD_ID;
    private static final String CATEGORY_KEY = KEY_PREFIX + ".category";

    public static final Lazy<KeyMapping> DISMOUNT = Lazy.of(() -> new KeyMapping(
            KEY_PREFIX + ".dismount",
            GLFW.GLFW_KEY_X,
            CATEGORY_KEY
    ));

    public static final Lazy<KeyMapping> UP = Lazy.of(() -> new KeyMapping(
            KEY_PREFIX + ".up",
            GLFW.GLFW_KEY_SPACE,
            CATEGORY_KEY
    ));

    public static final Lazy<KeyMapping> DOWN = Lazy.of(() -> new KeyMapping(
            KEY_PREFIX + ".down",
            GLFW.GLFW_KEY_LEFT_SHIFT,
            CATEGORY_KEY
    ));

    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(DISMOUNT.get());
        event.register(UP.get());
        event.register(DOWN.get());
    }
}
