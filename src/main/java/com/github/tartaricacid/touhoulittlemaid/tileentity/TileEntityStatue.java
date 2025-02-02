package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.init.InitBlocks;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityStatue extends BlockEntity {
    public static final BlockEntityType<TileEntityStatue> TYPE = BlockEntityType.Builder.of(TileEntityStatue::new, InitBlocks.STATUE.get()).build(null);
    private static final String STATUE_SIZE_TAG = "StatueSize";
    private static final String CORE_BLOCK_TAG = "CoreBlock";
    private static final String CORE_BLOCK_POS_TAG = "CoreBlockPos";
    private static final String STATUE_FACING_TAG = "StatueFacing";
    private static final String ALL_BLOCKS_TAG = "AllBlocks";
    private static final String EXTRA_MAID_DATA = "ExtraMaidData";
    private Size size = Size.SMALL;
    private boolean isCoreBlock = false;
    private BlockPos coreBlockPos = BlockPos.ZERO;
    private Direction facing = Direction.NORTH;
    private List<BlockPos> allBlocks = Lists.newArrayList();
    @Nullable
    private CompoundTag extraMaidData = null;

    public TileEntityStatue(BlockPos blockPos, BlockState blockState) {
        super(TYPE, blockPos, blockState);
    }

    public void setForgeData(Size size, boolean isCoreBlock, BlockPos coreBlockPos, Direction facing,
                             List<BlockPos> allBlocks, @Nullable CompoundTag extraData) {
        this.size = size;
        this.isCoreBlock = isCoreBlock;
        this.coreBlockPos = coreBlockPos;
        this.facing = facing;
        this.allBlocks = allBlocks;
        this.extraMaidData = extraData;
        refresh();
    }

    @Override
    public void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        getPersistentData().putInt(STATUE_SIZE_TAG, size.ordinal());
        getPersistentData().putBoolean(CORE_BLOCK_TAG, isCoreBlock);
        getPersistentData().put(CORE_BLOCK_POS_TAG, NbtUtils.writeBlockPos(coreBlockPos));
        getPersistentData().putString(STATUE_FACING_TAG, facing.getSerializedName());
        ListTag blockList = new ListTag();
        for (BlockPos pos : allBlocks) {
            blockList.add(NbtUtils.writeBlockPos(pos));
        }
        getPersistentData().put(ALL_BLOCKS_TAG, blockList);
        if (extraMaidData != null) {
            getPersistentData().put(EXTRA_MAID_DATA, extraMaidData);
        }
        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        size = Size.getSizeByIndex(getPersistentData().getInt(STATUE_SIZE_TAG));
        isCoreBlock = getPersistentData().getBoolean(CORE_BLOCK_TAG);
        NbtUtils.readBlockPos(getPersistentData(), CORE_BLOCK_POS_TAG).ifPresent(pos -> coreBlockPos = pos);
        facing = Direction.byName(getPersistentData().getString(STATUE_FACING_TAG));
        allBlocks.clear();
        ListTag blockList = getPersistentData().getList(ALL_BLOCKS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < blockList.size(); i++) {
            int[] pos = blockList.getIntArray(i);
            allBlocks.add(new BlockPos(pos[0], pos[1], pos[2]));
        }
        if (getPersistentData().contains(EXTRA_MAID_DATA, Tag.TAG_COMPOUND)) {
            extraMaidData = getPersistentData().getCompound(EXTRA_MAID_DATA);
        }
    }

    public BlockPos getWorldPosition() {
        return this.worldPosition;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return this.saveWithoutMetadata(pRegistries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void refresh() {
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    public Size getSize() {
        return size;
    }

    public boolean isCoreBlock() {
        return isCoreBlock;
    }

    public BlockPos getCoreBlockPos() {
        return coreBlockPos;
    }

    public Direction getFacing() {
        return facing;
    }

    public List<BlockPos> getAllBlocks() {
        return allBlocks;
    }

    @Nullable
    public CompoundTag getExtraMaidData() {
        return extraMaidData;
    }

    public enum Size {
        // 雕像的尺寸
        TINY(0.5f, new Vec3i(1, 1, 1)),
        SMALL(1.0f, new Vec3i(1, 2, 1)),
        MIDDLE(2.0f, new Vec3i(2, 4, 2)),
        BIG(3.0f, new Vec3i(3, 6, 3));

        private final float scale;
        private final Vec3i dimension;

        Size(float scale, Vec3i dimension) {
            this.scale = scale;
            this.dimension = dimension;
        }

        public static Size getSizeByIndex(int index) {
            return Size.values()[Mth.clamp(index, 0, Size.values().length - 1)];
        }

        public float getScale() {
            return scale;
        }

        public Vec3i getDimension() {
            return dimension;
        }
    }


}
