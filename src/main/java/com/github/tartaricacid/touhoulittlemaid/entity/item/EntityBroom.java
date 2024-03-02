package com.github.tartaricacid.touhoulittlemaid.entity.item;

import com.github.tartaricacid.touhoulittlemaid.capability.PowerCapability;
import com.github.tartaricacid.touhoulittlemaid.capability.PowerCapabilityProvider;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EntityBroom extends AbstractEntityFromItem implements HasCustomInventoryScreen, MenuProvider, OwnableEntity {
    public static final EntityType<EntityBroom> TYPE = EntityType.Builder.<EntityBroom>of(EntityBroom::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10).build("broom");

    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(EntityBroom.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final String OWNER_UUID_TAG = "OwnerUUID";
    private static final String CHEST_TAG = "BroomChest";

    private final SimpleContainer inventory = new SimpleContainer(27) {
        @Override
        public boolean stillValid(Player player) {
            return EntityBroom.this.isAlive();
        }
    };

    private LazyOptional<InvWrapper> itemHandler = LazyOptional.of(() -> new InvWrapper(inventory));
    private boolean keyForward = false;
    private boolean keyBack = false;
    private boolean keyLeft = false;
    private boolean keyRight = false;
    private boolean keyJump = false;

    public EntityBroom(EntityType<EntityBroom> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.setNoGravity(true);
    }

    public EntityBroom(Level worldIn) {
        this(TYPE, worldIn);
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyForward() {
        return Minecraft.getInstance().options.keyUp.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyBack() {
        return Minecraft.getInstance().options.keyDown.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyLeft() {
        return Minecraft.getInstance().options.keyLeft.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyRight() {
        return Minecraft.getInstance().options.keyRight.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keySpace() {
        return Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_ID, Optional.empty());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(OWNER_UUID_TAG)) {
            setOwnerUUID(NbtUtils.loadUUID(Objects.requireNonNull(compound.get(OWNER_UUID_TAG))));
        }
        if (compound.contains(CHEST_TAG, Tag.TAG_LIST)) {
            this.inventory.fromTag(compound.getList(CHEST_TAG, Tag.TAG_COMPOUND));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.entityData.get(OWNER_ID).ifPresent(uuid -> compound.putUUID(OWNER_UUID_TAG, uuid));
        compound.put(CHEST_TAG, this.inventory.createTag());
    }

    @Override
    public void travel(Vec3 vec3) {
        AABB aabb = this.getBoundingBox();
        int minX = Mth.floor(aabb.minX);
        int maxX = Mth.ceil(aabb.maxX);
        int y = Mth.floor(aabb.minY);
        int minZ = Mth.floor(aabb.minZ);
        int maxZ = Mth.ceil(aabb.maxZ);
        boolean flag1 = false;
        boolean flag2 = false;
        boolean flag3 = false;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        checkLoop1:
        for (int posX = minX; posX < maxX; posX++) {
            for (int posZ = minZ; posZ < maxZ; posZ++) {
                blockPos.set(posX, y - 1, posZ);
                BlockState blockState = this.level.getBlockState(blockPos);
                if (blockState.entityCanStandOn(this.level, blockPos, this)) {
                    flag2 = flag1 = true;
                    break checkLoop1;
                }
                FluidState fluidState = this.level.getFluidState(blockPos);
                if (!fluidState.isEmpty()) {
                    flag2 = true;
                }
            }
        }

        checkLoop2:
        for (int posX = minX; posX < maxX; posX++) {
            for (int posZ = minZ; posZ < maxZ; posZ++) {
                blockPos.set(posX, y, posZ);
                FluidState fluidState = this.level.getFluidState(blockPos);
                if (!fluidState.isEmpty()) {
                    flag3 = true;
                    break checkLoop2;
                }
            }
        }
        
        Entity entity = this.getControllingPassenger();
        if (entity instanceof Player player && this.isVehicle() && this.hasPassenger(e -> e instanceof EntityMaid)) {
            if (level.isClientSide) {
                // 不要问我为什么客户端数据能跑到服务端来
                // 一定是玄学
                keyForward = keyForward();
                keyBack = keyBack();
                keyLeft = keyLeft();
                keyRight = keyRight();
                keyJump = keySpace();
            }

            this.setOnGround(true);

            FavorabilityManager fManager = ((EntityMaid) this.getPassengers().get(1)).getFavorabilityManager();
    
            boolean flag4 = true;
            if (!flag1) {
                float powerRequired = fManager.getBroomPowerCost();
                PowerCapability power = player.getCapability(PowerCapabilityProvider.POWER_CAP, null).orElseThrow(null);
                flag4 = power.get() >= powerRequired;
                power.min(powerRequired);
            }
            
            double factorMu = fManager.getBroomFrictionFactor();
            this.setDeltaMovement(this.getDeltaMovement().multiply(factorMu, factorMu, factorMu));
            
            // 施加上下晃动
            this.addDeltaMovement(new Vec3(0, (0.0035 + 0.0005 * fManager.getLevel()) * Math.sin(this.tickCount * Math.PI / 30), 0));
            
            if (!flag2) {
                this.addDeltaMovement(new Vec3(0, flag4? -fManager.getBroomDownShiftingVelocity(): -0.1, 0));
            }

            if (flag3) {
                this.addDeltaMovement(new Vec3(0, 0.05, 0));
            }
            
            if (flag1 || flag4) {
                // 按键控制扫帚各个方向速度
                float forward = (keyForward ? 0.8f : 0) - (keyBack ? 0.4f : 0);
                float strafe = (keyLeft ? 0.3f : 0) - (keyRight ? 0.3f : 0);
                float vertical = keyJump ? 0.4f : 0;
                
                Vec3 vec31 = new Vec3(strafe, 0.0D, forward);
                vec31 = vec31.xRot((float) (-player.getXRot() * Math.PI / 400)).add(0, vertical, 0);
                
                this.moveRelative(fManager.getBroomVelocity(), vec31);
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }
        this.setXRot(0);

        // 施加上下晃动
        this.addDeltaMovement(new Vec3(0, 0.003 * Math.sin(this.tickCount * Math.PI / 30), 0));

        if (!flag2) {
            // 玩家没有坐在扫帚上，那就让它掉下来
            vec3 = vec3.add(0, -0.3, 0);
        }

        if (flag3) {
            vec3 = vec3.add(0, 0.15, 0);
        }

        super.travel(vec3);
    }

    @Override
    protected void pushEntities() {
        // 已经坐满两人，不执行
        if (this.getPassengers().size() >= 2) {
            return;
        }
        // 已经坐了一人，但不是玩家，不执行
        if (!this.getPassengers().isEmpty() && !(this.getControllingPassenger() instanceof Player)) {
            return;
        }
        if (!level.isClientSide) {
            List<EntityMaid> list = level.getEntitiesOfClass(EntityMaid.class, getBoundingBox().expandTowards(0.5, 0.1, 0.5), this::canMaidRide);
            list.stream().findFirst().ifPresent(entity -> entity.startRiding(this));
        }
    }

    private boolean canMaidRide(EntityMaid maid) {
        if (maid.canBrainMoving() && !maid.isVehicle() && EntitySelector.pushableBy(this).test(maid)) {
            UUID maidOwnerUUID = maid.getOwnerUUID();
            UUID broomOwnerUUID = this.getOwnerUUID();
            if (maidOwnerUUID == null || broomOwnerUUID == null) {
                return false;
            }
            return maidOwnerUUID.equals(broomOwnerUUID);
        }
        return false;
    }

    @Override
    protected void tickRidden(Player player, Vec3 pTravelVector) {
        // 记得将 fall distance 设置为 0，否则会摔死
        this.fallDistance = 0;

        // 与旋转有关系的一堆东西，用来控制扫帚朝向
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.setRot(player.getYRot(), player.getXRot());
        super.tickRidden(player, pTravelVector);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            double xOffset = passenger instanceof EntityMaid ? -0.5 : 0;
            double yOffset = this.isRemoved() ? 0.01 : this.getPassengersRidingOffset() + passenger.getMyRidingOffset();
            if (this.getPassengers().size() > 1) {
                int passengerIndex = this.getPassengers().indexOf(passenger);
                if (passengerIndex == 0) {
                    xOffset = 0.35;
                } else {
                    xOffset = -0.35;
                }
            }
            Vec3 offset = new Vec3(xOffset, yOffset, 0).yRot((float) (-this.getYRot() * Math.PI / 180 - Math.PI / 2)).xRot((float) (-this.getXRot() * Math.PI / 810));
            moveFunction.accept(passenger, this.getX() + offset.x, this.getY() + offset.y, this.getZ() + offset.z);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.isDiscrete() && !this.isPassenger() && !(this.getControllingPassenger() instanceof Player)) {
            if (this.getPassengers().size() > 1) {
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            if (player.getUUID().equals(this.getOwnerUUID())) {
                if (!level.isClientSide) {
                    player.startRiding(this);
                }
            } else {
                if (hand == InteractionHand.MAIN_HAND && this.level.isClientSide) {
                    player.sendSystemMessage(Component.translatable("message.touhou_little_maid.broom.not_the_owner"));
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        if (player.isSecondaryUseActive()) {
            if (player.getUUID().equals(this.getOwnerUUID())) {
                this.openCustomInventoryScreen(player);
            } else {
                if (hand == InteractionHand.MAIN_HAND && this.level.isClientSide) {
                    player.sendSystemMessage(Component.translatable("message.touhou_little_maid.broom.not_the_owner"));
                }
                return InteractionResult.FAIL;
            }
        }

        return super.interact(player, hand);
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level.isClientSide && player.getUUID().equals(this.getOwnerUUID())) {
            player.openMenu(this);
        }
    }

    @Override
    protected void dropExtraItems() {
        if (this.inventory != null) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack stack = this.inventory.getItem(i);
                if (!stack.isEmpty()) {
                    this.spawnAtLocation(stack);
                }
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return ChestMenu.threeRows(pContainerId, pPlayerInventory, this.inventory);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (this.isAlive() && capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(() -> new InvWrapper(this.inventory));
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < 2;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0;
    }

    @Override
    protected boolean canKillEntity(Player player) {
        return player.getUUID().equals(this.getOwnerUUID());
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.WOOL_BREAK;
    }

    @Override
    protected Item getWithItem() {
        return InitItems.BROOM.get();
    }

    @Override
    protected ItemStack getKilledStack() {
        return new ItemStack(this.getWithItem());
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        this.resetFallDistance();
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_ID, Optional.ofNullable(uuid));
    }

    public SimpleContainer getInventory() {
        return inventory;
    }
}
