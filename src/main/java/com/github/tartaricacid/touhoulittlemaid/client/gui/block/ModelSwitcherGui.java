package com.github.tartaricacid.touhoulittlemaid.client.gui.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.cache.CacheIconManager;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.DirectButton;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.ImageButtonWithId;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.TouhouImageButton;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.message.SaveSwitcherDataPackage;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import com.github.tartaricacid.touhoulittlemaid.util.ParseI18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

public class ModelSwitcherGui extends Screen {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/gui/model_switcher.png");
    private static final ResourceLocation DEFAULT_MODEL_ID = ResourceLocation.parse("touhou_little_maid:hakurei_reimu");
    private final List<TileEntityModelSwitcher.ModeInfo> infoList;
    private final BlockPos pos;
    private final int maxRow = 6;
    private final UUID bindUuid;
    protected int imageWidth = 256;
    protected int imageHeight = 166;
    protected int leftPos;
    protected int topPos;
    private EntityMaid maid = null;
    private EditBox description;
    private int selectedIndex = -1;
    private int page;

    public ModelSwitcherGui(TileEntityModelSwitcher switcher) {
        super(Component.literal("Model Switcher GUI"));
        this.infoList = switcher.getInfoList();
        this.pos = switcher.getBlockPos();
        this.bindUuid = switcher.getUuid();
        if (Minecraft.getInstance().level != null) {
            this.maid = new EntityMaid(Minecraft.getInstance().level);
        }
    }

    @Override
    protected void init() {
        this.clearWidgets();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.selectedIndex = selectedIndex < infoList.size() ? selectedIndex : -1;
        this.page = page <= (infoList.size() - 1) / maxRow ? page : 0;

        this.addListChangeButton();
        this.addPageButton();
        this.addListButton();
        if (selectedIndex >= 0) {
            this.addEditButton();
        } else {
            this.description = null;
        }
    }

    private void addEditButton() {
        TileEntityModelSwitcher.ModeInfo info = this.infoList.get(selectedIndex);
        maid.setModelId(info.getModelId().toString());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.touhou_little_maid.button.skin"), b -> CacheIconManager.openModelSwitcherModelGui(maid, info, this))
                .pos(leftPos + 55, topPos + 15).size(76, 20).build());

        this.addRenderableWidget(new DirectButton(leftPos + 55, topPos + 38, 76, 20, info.getDirection(),
                b -> info.setDirection(((DirectButton) b).getDirection())));

        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.save"), b -> {
            PacketDistributor.sendToServer(new SaveSwitcherDataPackage(pos, this.infoList));
        }).pos(leftPos + 12, topPos + 135).size(121, 20).build());

        this.description = new EditBox(getMinecraft().font, leftPos + 12, topPos + 65, 119, 20,
                Component.translatable("gui.touhou_little_maid.name_tag.edit_box"));
        this.description.setValue(info.getText());
        this.addWidget(this.description);
        this.setInitialFocus(this.description);
    }

    private void addListButton() {
        int startOffsetY = topPos + 24;
        for (int i = page * maxRow; i < Math.min(infoList.size(), (page + 1) * maxRow); i++) {
            ImageButtonWithId button;
            if (i != selectedIndex) {
                button = new ImageButtonWithId(i, leftPos + 141, startOffsetY, 108, 19, 0, 166, 19, BG, b -> {
                    selectedIndex = ((ImageButtonWithId) b).getIndex();
                    this.init();
                });
            } else {
                button = new ImageButtonWithId(i, leftPos + 141, startOffsetY, 108, 19, 108, 166, 0, BG, b -> {
                    selectedIndex = -1;
                    this.init();
                });
            }
            this.addRenderableWidget(button);
            startOffsetY += 19;
        }
    }

    private void addPageButton() {
        this.addRenderableWidget(new TouhouImageButton(leftPos + 141, topPos + 7, 13, 16, 0, 204, 16, BG, b -> {
            if (page > 0) {
                page = page - 1;
                this.init();
            }
        }));
        this.addRenderableWidget(new TouhouImageButton(leftPos + 236, topPos + 7, 13, 16, 13, 204, 16, BG, b -> {
            if ((page + 1) <= (infoList.size() - 1) / maxRow) {
                page = page + 1;
                this.init();
            }
        }));
    }

    private void addListChangeButton() {
        this.addRenderableWidget(Button.builder(Component.translatable("gui.touhou_little_maid.model_switcher.list.add"), b -> {
            this.infoList.add(new TileEntityModelSwitcher.ModeInfo(DEFAULT_MODEL_ID, "", Direction.NORTH));
            this.init();
        }).pos(leftPos + 141, topPos + 139).size(53, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.deleteButton"), b -> {
            if (-1 < selectedIndex && selectedIndex < this.infoList.size()) {
                this.infoList.remove(selectedIndex);
                selectedIndex = -1;
                this.init();
            }
        }).pos(leftPos + 196, topPos + 139).size(53, 20).build());
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        String value = "";
        if (this.description != null) {
            value = this.description.getValue();
        }
        super.resize(pMinecraft, pWidth, pHeight);
        if (this.description != null) {
            this.description.setValue(value);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.maid == null) {
            return;
        }
        this.renderBackground(graphics, pMouseX, pMouseY, pPartialTick);
        graphics.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (bindUuid != null) {
            graphics.drawCenteredString(font, bindUuid.toString(), leftPos + 128, topPos - 10, 0xffffff);
        } else {
            graphics.drawCenteredString(font, Component.translatable("gui.touhou_little_maid.model_switcher.uuid.empty"), leftPos + 128, topPos - 10, 0xffffff);
        }
        graphics.drawCenteredString(font, String.format("%d/%d", page + 1, (infoList.size() - 1) / maxRow + 1), leftPos + 193, topPos + 12, 0xffffff);
        if (this.description != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    leftPos + 9,
                    topPos + 8,
                    leftPos + 54,
                    topPos + 68,
                    24,
                    0.1F,
                    leftPos + 45,
                    topPos + 45,
                    maid);
            this.description.render(graphics, pMouseX, pMouseY, pPartialTick);
        }
        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, pMouseX, pMouseY, pPartialTick);
        }
        this.renderListButtonName(graphics);
    }

    private void renderListButtonName(GuiGraphics graphics) {
        int startOffsetY = topPos + 29;
        for (int i = page * maxRow; i < Math.min(infoList.size(), (page + 1) * maxRow); i++) {
            String modelId = infoList.get(i).getModelId().toString();
            if (CustomPackLoader.MAID_MODELS.getInfo(modelId).isPresent()) {
                MaidModelInfo info = CustomPackLoader.MAID_MODELS.getInfo(modelId).get();
                MutableComponent component = Component.translatable(ParseI18n.getI18nKey(info.getName()));
                graphics.drawCenteredString(font, component, leftPos + 193, startOffsetY, 0xffffff);
            }
            startOffsetY += 19;
        }
    }

    @Override
    public void tick() {
        if (this.description != null) {
            if (0 <= selectedIndex && selectedIndex < infoList.size()) {
                infoList.get(selectedIndex).setText(description.getValue());
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.description != null && this.description.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.description);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (this.description != null) {
            if (overwrite) {
                this.description.setValue(text);
            } else {
                this.description.insertText(text);
            }
        }
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new SaveSwitcherDataPackage(pos, this.infoList));
        super.onClose();
    }
}
