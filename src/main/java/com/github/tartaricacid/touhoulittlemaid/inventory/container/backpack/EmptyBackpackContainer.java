package com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack;

import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public class EmptyBackpackContainer extends MaidMainContainer {
    public static final MenuType<EmptyBackpackContainer> TYPE = IMenuTypeExtension.create((windowId, inv, data) -> new EmptyBackpackContainer(windowId, inv, data.readInt()));

    public EmptyBackpackContainer(int id, Inventory inventory, int entityId) {
        super(TYPE, id, inventory, entityId);
    }

    @Override
    protected void addBackpackInv(Inventory inventory) {
    }
}