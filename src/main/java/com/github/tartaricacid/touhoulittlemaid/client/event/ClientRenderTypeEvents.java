package com.github.tartaricacid.touhoulittlemaid.client.event;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TouhouLittleMaid.MOD_ID, value = Dist.CLIENT)
public class ClientRenderTypeEvents {
    private static ShaderInstance shaderInstance;

    private static final Supplier<RenderType> VANILLA_ITEM_ENTITY_CHUNK_RENDER_TYPE_SUPPLIER = Suppliers.memoize(() -> RenderType.create("touhou_little_maid:item_entity_chunk", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> ClientRenderTypeEvents.shaderInstance))
            .setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setOverlayState(RenderStateShard.OVERLAY)
            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
            .createCompositeState(true)
    ));

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "rendertype_item_entity_chunk"), DefaultVertexFormat.NEW_ENTITY), ClientRenderTypeEvents::setShaderInstance);
    }

    private static void setShaderInstance(ShaderInstance shaderInstance) {
        ClientRenderTypeEvents.shaderInstance = shaderInstance;
    }

    public static RenderType get() {
        return VANILLA_ITEM_ENTITY_CHUNK_RENDER_TYPE_SUPPLIER.get();
    }
}
