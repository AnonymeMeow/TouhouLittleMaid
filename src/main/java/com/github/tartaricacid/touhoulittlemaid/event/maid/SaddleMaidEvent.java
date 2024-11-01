package com.github.tartaricacid.touhoulittlemaid.event.maid;

import com.github.tartaricacid.touhoulittlemaid.advancements.maid.TriggerType;
import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;

@EventBusSubscriber
public class SaddleMaidEvent {
    @SubscribeEvent
    public static void onInteract(InteractMaidEvent event) {
        Player player = event.getPlayer();
        EntityMaid maid = event.getMaid();
        ItemStack stack = event.getStack();
        if (stack.is(Items.SADDLE)) {
            if (player.getPassengers().isEmpty() && maid.getPassengers().isEmpty()) {
                boolean success = maid.startRiding(player);
                if (success && FMLLoader.getDist() == Dist.CLIENT) {
                    SaddleMaidEvent.showTips();
                }
                if (maid.isHomeModeEnable()) {
                    maid.setHomeModeEnable(false);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    InitTrigger.MAID_EVENT.get().trigger(serverPlayer, TriggerType.PICKUP_MAID);
                }
                event.setCanceled(true);
                return;
            }
            if (!player.getPassengers().isEmpty()) {
                player.ejectPassengers();
                event.setCanceled(true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void showTips() {
        Minecraft minecraft = Minecraft.getInstance();
        Component component = Component.translatable("message.touhou_little_maid.saddle.how_to_eject");
        minecraft.gui.setOverlayMessage(component, false);
        minecraft.getNarrator().sayNow(component);
    }
}
