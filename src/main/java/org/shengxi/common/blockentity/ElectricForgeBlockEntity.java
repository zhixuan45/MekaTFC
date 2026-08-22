package org.shengxi.common.blockentity;

import net.dries007.tfc.common.blocks.devices.Tiered;
import net.dries007.tfc.common.blocks.rock.RockAnvilBlock;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.Forging;
import net.dries007.tfc.common.capabilities.forge.ForgingCapability;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shengxi.common.block.ElectricForgeBlock;
import org.shengxi.common.container.ElectricForgeMenu;
import org.shengxi.common.energy.CustomEnergyStorage;
import org.shengxi.common.util.AutoForgeSolver;
import org.shengxi.registry.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动锻造机方块实体 (ElectricForgeBlockEntity)
 * 具备 Mekanism 电力接入、智能电热温控、铁砧模具等级切换以及基于算法的自动锻造与远程击打功能。
 */
public class ElectricForgeBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {

    // 槽位索引定义（对应 TFC 经典布局 + Mekanism 升级管理双槽）
    public static final int SLOT_MAIN = 0;            // 主工件槽 (对应原版左侧主输入)
    public static final int SLOT_SECOND = 1;          // 副工件槽 (对应原版左侧副输入/焊接)
    public static final int SLOT_ANVIL = 2;           // 铁砧模具槽 (对应原版右侧锤子槽，用于提供工艺等级)
    public static final int SLOT_CATALYST = 3;        // 助焊剂槽 (对应原版右侧助焊剂槽)
    public static final int SLOT_UPGRADE_INPUT = 4;   // 升级安装输入槽
    public static final int SLOT_UPGRADE_OUTPUT = 5;  // 升级卸载输出槽
    public static final int TOTAL_SLOTS = 6;

    // 能耗与时钟常量
    public static final int BASE_CAPACITY = 100_000;
    public static final int CAPACITY_PER_ENERGY_UPGRADE = 50_000;
    public static final int MAX_TRANSFER = 10_000;
    public static final int HEATING_ENERGY_COST = 60;   // 基础升温每 Tick 耗电 (FE/t)
    public static final int HOLDING_ENERGY_COST = 5;    // 基础保温每 Tick 耗电 (FE/t)
    public static final int FORGE_STEP_ENERGY_COST = 200; // 基础单次击打耗电 (FE)
    public static final int BASE_FORGE_INTERVAL = 20;   // 基础自动击打间隔 (20 ticks = 1秒)
    public static final int UPGRADE_TICKS_REQUIRED = 20; // 安装升级所需 Tick 数 (20 ticks = 1秒)

    // 工作状态码
    public static final int STATE_IDLE = 0;
    public static final int STATE_HEATING = 1;
    public static final int STATE_HOLDING = 2;
    public static final int STATE_FORGING = 3;

    // 物品清单
    protected final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    // 升级管理集合与安装计时
    private final java.util.Map<org.shengxi.common.upgrade.ForgeUpgradeType, Integer> installedUpgrades = new java.util.EnumMap<>(org.shengxi.common.upgrade.ForgeUpgradeType.class);
    private int upgradeTicks = 0;

    // 能量存储
    protected final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energyCapability;

    // 自动化物品能力包装
    protected final LazyOptional<net.minecraftforge.items.IItemHandlerModifiable>[] itemHandlers = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    // 状态字段
    private boolean autoForgeEnabled = true;
    private int autoForgeTimer = 0;
    private int currentState = STATE_IDLE;
    private String selectedRecipeId = "";
    private boolean recipeLocked = false;
    private String lockedRecipeId = "";
    private boolean autoEject = true;
    // 6 个面的模式配置 (0: INPUT, 1: OUTPUT, 2: ENERGY_ONLY, 3: DISABLED)
    // 对应 Direction.values() 顺序: DOWN(0), UP(1), NORTH(2), SOUTH(3), WEST(4), EAST(5)
    private final int[] sideConfigs = new int[]{1, 0, 0, 0, 0, 0}; // 默认 DOWN 输出，其他面输入
    private List<ForgeStep> cachedSteps = new ArrayList<>();

    // ContainerData 用于与客户端 Menu 实时同步数值
    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> getTier();
                case 3 -> autoForgeEnabled ? 1 : 0;
                case 4 -> currentState;
                case 5 -> autoForgeTimer;
                case 6 -> (int) getMainItemTemperature();
                case 7 -> (int) getMainItemWorkingTemperature();
                case 8 -> recipeLocked ? 1 : 0;
                case 9 -> upgradeTicks;
                case 10 -> getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.SPEED);
                case 11 -> getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.ENERGY);
                case 12 -> getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.MUFFLING);
                case 13 -> getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.PERFECT_FORGING);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 3 -> autoForgeEnabled = (value == 1);
                case 4 -> currentState = value;
                case 5 -> autoForgeTimer = value;
                case 8 -> recipeLocked = (value == 1);
                case 9 -> upgradeTicks = value;
                case 10 -> setClientUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.SPEED, value);
                case 11 -> setClientUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.ENERGY, value);
                case 12 -> setClientUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.MUFFLING, value);
                case 13 -> setClientUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.PERFECT_FORGING, value);
            }
        }

        @Override
        public int getCount() {
            return 14;
        }
    };

    public ElectricForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FORGE.get(), pos, state);
        this.energyStorage = new CustomEnergyStorage(BASE_CAPACITY, MAX_TRANSFER, this::setChanged);
        this.energyCapability = LazyOptional.of(() -> this.energyStorage);
    }


    /**
     * 获取指定类型的已安装升级数量
     */
    public int getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType type) {
        return installedUpgrades.getOrDefault(type, 0);
    }

    /**
     * 客户端专用：设置升级数量以供渲染
     */
    private void setClientUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType type, int count) {
        if (count <= 0) {
            installedUpgrades.remove(type);
        } else {
            installedUpgrades.put(type, count);
        }
    }

    /**
     * 安装升级
     */
    public int addUpgrade(org.shengxi.common.upgrade.ForgeUpgradeType type, int count) {
        int current = getUpgradeCount(type);
        int toAdd = Math.min(type.getMaxCount() - current, count);
        if (toAdd > 0) {
            installedUpgrades.put(type, current + toAdd);
            recalculateEnergyStorage();
            setChanged();
            return toAdd;
        }
        return 0;
    }

    /**
     * 卸载升级至输出槽
     */
    public void uninstallUpgrade(org.shengxi.common.upgrade.ForgeUpgradeType type, boolean removeAll) {
        if (level == null || level.isClientSide) return;
        int installed = getUpgradeCount(type);
        if (installed <= 0) return;
        int toRemove = removeAll ? installed : 1;
        ItemStack outStack = items.get(SLOT_UPGRADE_OUTPUT);
        ItemStack template = type.createStack(1);
        if (template.isEmpty()) return;

        if (outStack.isEmpty()) {
            int canAdd = Math.min(toRemove, template.getMaxStackSize());
            items.set(SLOT_UPGRADE_OUTPUT, type.createStack(canAdd));
            int remain = installed - canAdd;
            if (remain <= 0) {
                installedUpgrades.remove(type);
            } else {
                installedUpgrades.put(type, remain);
            }
            recalculateEnergyStorage();
            setChanged();
        } else if (ItemStack.isSameItemSameTags(outStack, template)) {
            int maxAdd = Math.min(toRemove, outStack.getMaxStackSize() - outStack.getCount());
            if (maxAdd > 0) {
                outStack.grow(maxAdd);
                int remain = installed - maxAdd;
                if (remain <= 0) {
                    installedUpgrades.remove(type);
                } else {
                    installedUpgrades.put(type, remain);
                }
                recalculateEnergyStorage();
                setChanged();
            }
        }
    }

    /**
     * 根据已安装能量升级重新计算能量上限
     */
    public void recalculateEnergyStorage() {
        int energyUpgrades = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.ENERGY);
        int newCapacity = BASE_CAPACITY + energyUpgrades * CAPACITY_PER_ENERGY_UPGRADE;
        this.energyStorage.setCapacity(newCapacity);
    }

    /**
     * 获取自动击打间隔 (ticks)
     */
    public int getForgeInterval() {
        int speedUpgrades = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.SPEED);
        return Math.max(2, (int) (BASE_FORGE_INTERVAL / (1.0 + speedUpgrades * 0.5)));
    }

    /**
     * 获取升温功耗 (FE/t)
     */
    public int getHeatingEnergyCost() {
        int speed = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.SPEED);
        int energy = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.ENERGY);
        double factor = 1.0 + speed * 0.15 - energy * 0.08;
        return Math.max(10, (int) (HEATING_ENERGY_COST * factor));
    }

    /**
     * 获取保温功耗 (FE/t)
     */
    public int getHoldingEnergyCost() {
        int energy = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.ENERGY);
        double factor = 1.0 - energy * 0.08;
        return Math.max(1, (int) (HOLDING_ENERGY_COST * factor));
    }

    /**
     * 获取单次击打功耗 (FE)
     */
    public int getForgeStepEnergyCost() {
        int speed = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.SPEED);
        int energy = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.ENERGY);
        double factor = 1.0 + speed * 0.1 - energy * 0.08;
        return Math.max(30, (int) (FORGE_STEP_ENERGY_COST * factor));
    }

    /**
     * 获取升温步长 (°C/t)
     */
    public float getHeatingRate() {
        int speed = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.SPEED);
        return 12.0f * (1.0f + speed * 0.25f);
    }

    /**
     * 获取消音音量系数 (0.0 ~ 1.0)
     */
    public float getMufflingVolumeFactor() {
        int muffling = getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.MUFFLING);
        return Math.max(0.0f, 1.0f - muffling * 0.25f);
    }

    /**
     * 获取当前安装的铁砧等级（-1 为未安装，0 为石砧，1~6 为各金属砧）
     */
    public int getTier() {
        ItemStack anvilStack = items.get(SLOT_ANVIL);
        if (anvilStack.isEmpty()) {
            return -1;
        }
        if (anvilStack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof Tiered tiered) {
                return tiered.getTier();
            } else if (block instanceof RockAnvilBlock) {
                return 0;
            }
        }
        return -1;
    }

    /**
     * 获取主槽工件当前温度
     */
    public float getMainItemTemperature() {
        ItemStack mainStack = items.get(SLOT_MAIN);
        if (!mainStack.isEmpty()) {
            IHeat heat = HeatCapability.get(mainStack);
            if (heat != null) {
                return heat.getTemperature();
            }
        }
        return 0.0f;
    }

    /**
     * 获取主槽工件的可工作温度
     */
    public float getMainItemWorkingTemperature() {
        ItemStack mainStack = items.get(SLOT_MAIN);
        if (!mainStack.isEmpty()) {
            IHeat heat = HeatCapability.get(mainStack);
            if (heat != null) {
                return heat.getWorkingTemperature();
            }
        }
        return 0.0f;
    }

    /**
     * 服务端 Tick 循环逻辑
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricForgeBlockEntity entity) {
        if (level == null || level.isClientSide) {
            return;
        }

        // 1. 升级安装读条处理 (Mekanism 机制：输入槽放入支持的芯片，满 20 ticks 自动安装)
        ItemStack upgradeIn = entity.items.get(SLOT_UPGRADE_INPUT);
        if (!upgradeIn.isEmpty()) {
            org.shengxi.common.upgrade.ForgeUpgradeType type = org.shengxi.common.upgrade.ForgeUpgradeType.fromItemStack(upgradeIn);
            if (type != null && entity.getUpgradeCount(type) < type.getMaxCount()) {
                if (entity.upgradeTicks < UPGRADE_TICKS_REQUIRED) {
                    entity.upgradeTicks++;
                } else {
                    entity.addUpgrade(type, 1);
                    upgradeIn.shrink(1);
                    entity.upgradeTicks = 0;
                    entity.setChanged();
                }
            } else {
                entity.upgradeTicks = 0;
            }
        } else {
            entity.upgradeTicks = 0;
        }

        ItemStack mainStack = entity.items.get(SLOT_MAIN);
        int oldState = entity.currentState;
        entity.currentState = STATE_IDLE;

        // 2. 智能电热温控逻辑
        if (!mainStack.isEmpty()) {
            IHeat heat = HeatCapability.get(mainStack);
            if (heat != null) {
                float currentTemp = heat.getTemperature();
                float workingTemp = heat.getWorkingTemperature();
                float weldingTemp = heat.getWeldingTemperature();

                // 判断是否需要焊接温度
                boolean needsWeld = !entity.items.get(SLOT_SECOND).isEmpty() && !entity.items.get(SLOT_CATALYST).isEmpty();
                float targetTemp = needsWeld ? weldingTemp : workingTemp;

                if (targetTemp > 0) {
                    if (currentTemp < targetTemp) {
                        // 升温模式
                        int heatingCost = entity.getHeatingEnergyCost();
                        if (entity.energyStorage.getEnergyStored() >= heatingCost) {
                            entity.energyStorage.consumeEnergy(heatingCost);
                            // 平滑提升温度
                            heat.setTemperature(Math.min(targetTemp, currentTemp + entity.getHeatingRate()));
                            entity.currentState = STATE_HEATING;
                        }
                    } else {
                        // 保温模式（恒温维持）
                        int holdingCost = entity.getHoldingEnergyCost();
                        if (entity.energyStorage.getEnergyStored() >= holdingCost) {
                            entity.energyStorage.consumeEnergy(holdingCost);
                            heat.setTemperature(Math.max(currentTemp, targetTemp));
                            entity.currentState = STATE_HOLDING;
                        }
                    }
                }
            }

            // 自动继承已锁定的配方
            if (entity.recipeLocked && !entity.lockedRecipeId.isEmpty()) {
                Forging forge = ForgingCapability.get(mainStack);
                if (forge != null && forge.getRecipe(level) == null) {
                    ResourceLocation resId = ResourceLocation.tryParse(entity.lockedRecipeId);
                    if (resId != null) {
                        var opt = level.getRecipeManager().byKey(resId);
                        if (opt.isPresent() && opt.get() instanceof AnvilRecipe lockedRecipe) {
                            if (lockedRecipe.getInput().test(mainStack)) {
                                entity.chooseRecipe(lockedRecipe);
                            }
                        }
                    }
                }
            }
        }

        // 3. 自动锻造循环逻辑
        if (entity.autoForgeEnabled && !mainStack.isEmpty() && entity.getTier() >= 0) {
            Forging forge = ForgingCapability.get(mainStack);
            IHeat heat = HeatCapability.get(mainStack);

            if (forge != null && heat != null && heat.canWork()) {
                AnvilRecipe recipe = entity.resolveRecipe(forge);
                if (recipe != null && recipe.isCorrectTier(entity.getTier())) {
                    entity.currentState = STATE_FORGING;
                    var inv = new AnvilRecipeInventory(mainStack, entity.getTier());

                    // 检查是否已经完成
                    if (recipe.checkComplete(inv)) {
                        entity.checkAndAssembleRecipe(forge, recipe, heat);
                        entity.setChanged();
                    } else {
                        // 若未完成且无缓存步骤，重新求解路径
                        if (entity.cachedSteps.isEmpty()) {
                            entity.cachedSteps = AutoForgeSolver.solve(
                                    forge.getWork(),
                                    forge.getWorkTarget(),
                                    forge.getSteps(),
                                    recipe.getRules()
                            );
                        }

                        if (!entity.cachedSteps.isEmpty()) {
                            if (entity.autoForgeTimer <= 0) {
                                int stepCost = entity.getForgeStepEnergyCost();
                                if (entity.energyStorage.getEnergyStored() >= stepCost) {
                                    entity.energyStorage.consumeEnergy(stepCost);
                                    ForgeStep stepToRun = entity.cachedSteps.remove(0);

                                    // 执行打击 (调用单参数 addStep，正确应用 step 的偏移量)
                                    forge.addStep(stepToRun);
                                    entity.autoForgeTimer = entity.getForgeInterval();

                                    // 播放音效与火花粒子 (受消音升级影响)
                                    float volume = 0.8f * entity.getMufflingVolumeFactor();
                                    if (volume > 0.05f) {
                                        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, volume, 1.1f);
                                        if (level instanceof ServerLevel serverLevel) {
                                            serverLevel.sendParticles(ParticleTypes.CRIT,
                                                    pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                                                    5, 0.15, 0.1, 0.15, 0.05);
                                        }
                                    }

                                    // 每次击打后立刻检验是否完成
                                    entity.checkAndAssembleRecipe(forge, recipe, heat);
                                    entity.setChanged();
                                }
                            } else {
                                entity.autoForgeTimer--;
                            }
                        }
                    }
                }
            }
        }

        // 更新方块激活亮起状态
        boolean isWorking = entity.currentState != STATE_IDLE;
        if (state.hasProperty(ElectricForgeBlock.WORKING) && state.getValue(ElectricForgeBlock.WORKING) != isWorking) {
            level.setBlock(pos, state.setValue(ElectricForgeBlock.WORKING, isWorking), Block.UPDATE_ALL);
        }

        if (oldState != entity.currentState) {
            entity.setChanged();
        }
    }

    /**
     * 检验是否安装了完美锻造升级模块
     */
    public boolean hasPerfectForgingUpgrade() {
        return getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType.PERFECT_FORGING) > 0;
    }


    /**
     * 解析当前工件绑定的图纸配方
     */
    @Nullable
    public AnvilRecipe resolveRecipe(Forging forge) {
        if (level == null) return null;
        AnvilRecipe recipe = forge.getRecipe(level);
        if (recipe != null) {
            return recipe;
        }
        if (!selectedRecipeId.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(selectedRecipeId);
            if (id != null) {
                var found = level.getRecipeManager().byKey(id);
                if (found.isPresent() && found.get() instanceof AnvilRecipe anvilRecipe) {
                    return anvilRecipe;
                }
            }
        }
        return null;
    }

    /**
     * 检验锻造是否完成并生成产物
     */
    private void checkAndAssembleRecipe(Forging forge, AnvilRecipe recipe, IHeat heat) {
        if (level == null) return;
        boolean matchesTarget = (forge.getWork() == forge.getWorkTarget());
        boolean matchesRules = forge.matches(recipe.getRules());

        if (matchesTarget && matchesRules) {
            // 组装成品
            ItemStack outputStack = recipe.assemble(new AnvilRecipeInventory(items.get(SLOT_MAIN), getTier()), level.registryAccess());

            if (!outputStack.isEmpty()) {
                // 继承温度
                IHeat outputHeat = HeatCapability.get(outputStack);
                if (outputHeat != null && heat != null) {
                    outputHeat.setTemperature(heat.getTemperature());
                }

                // 完美锻造升级效果：赋予最高品质完美锻造等级
                if (hasPerfectForgingUpgrade()) {
                    net.dries007.tfc.common.capabilities.forge.ForgingBonus.set(
                            outputStack,
                            net.dries007.tfc.common.capabilities.forge.ForgingBonus.PERFECTLY_FORGED
                    );
                }

                // 产出成品替换主槽（与 TFC 铁砧行为完全一致）
                items.set(SLOT_MAIN, outputStack);

                // 播放完成特效与音效
                level.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.9f, 1.4f);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            worldPosition.getX() + 0.5, worldPosition.getY() + 0.9, worldPosition.getZ() + 0.5,
                            8, 0.2, 0.1, 0.2, 0.05);
                }

                cachedSteps.clear();
            }
        }
    }

    /**
     * 设定锻造图纸配方
     */
    public void chooseRecipe(AnvilRecipe recipe) {
        ItemStack mainStack = items.get(SLOT_MAIN);
        if (!mainStack.isEmpty() && recipe != null) {
            Forging forge = ForgingCapability.get(mainStack);
            if (forge != null) {
                forge.setRecipe(recipe, new AnvilRecipeInventory(mainStack, getTier()));
                this.selectedRecipeId = recipe.getId().toString();
                if (this.recipeLocked) {
                    this.lockedRecipeId = this.selectedRecipeId;
                }
                this.cachedSteps.clear();
                setChanged();
            }
        }
    }

    /**
     * 铁砧配方输入容器包装实现
     */
    public record AnvilRecipeInventory(ItemStack stack, int tier) implements AnvilRecipe.Inventory {
        @Override
        public ItemStack getItem() {
            return stack;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public long getSeed() {
            return 0L;
        }
    }

    /**
     * 切换配方锁定状态
     */
    public void toggleRecipeLock() {
        this.recipeLocked = !this.recipeLocked;
        if (this.recipeLocked && !this.selectedRecipeId.isEmpty()) {
            this.lockedRecipeId = this.selectedRecipeId;
        }
        setChanged();
    }

    public boolean isRecipeLocked() {
        return this.recipeLocked;
    }

    /**
     * 手动执行一步击打
     */
    public boolean performManualStep(ForgeStep step) {
        if (level == null || level.isClientSide) return false;
        ItemStack mainStack = items.get(SLOT_MAIN);
        if (mainStack.isEmpty() || getTier() < 0) return false;

        Forging forge = ForgingCapability.get(mainStack);
        IHeat heat = HeatCapability.get(mainStack);

        if (forge != null && heat != null && heat.canWork()) {
            if (energyStorage.getEnergyStored() >= FORGE_STEP_ENERGY_COST) {
                energyStorage.consumeEnergy(FORGE_STEP_ENERGY_COST);
                forge.addStep(step);

                level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8f, 1.1f);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            worldPosition.getX() + 0.5, worldPosition.getY() + 0.9, worldPosition.getZ() + 0.5,
                            4, 0.1, 0.1, 0.1, 0.05);
                }

                AnvilRecipe recipe = resolveRecipe(forge);
                if (recipe != null) {
                    checkAndAssembleRecipe(forge, recipe, heat);
                }

                cachedSteps.clear();
                setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * 切换自动锻造开关
     */
    public void setAutoForgeEnabled(boolean enabled) {
        this.autoForgeEnabled = enabled;
        this.cachedSteps.clear();
        setChanged();
    }

    public boolean isAutoForgeEnabled() {
        return autoForgeEnabled;
    }

    /**
     * 设定目标配方图纸 ID
     */
    public void setSelectedRecipe(String recipeId) {
        this.selectedRecipeId = recipeId != null ? recipeId : "";
        this.cachedSteps.clear();
        setChanged();
    }

    public String getSelectedRecipeId() {
        return selectedRecipeId;
    }

    public CustomEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    // ========== Container / WorldlyContainer 实现 ==========

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            cachedSteps.clear();
            setChanged();
        }
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        cachedSteps.clear();
        return result;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        cachedSteps.clear();
        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.clear();
        cachedSteps.clear();
        setChanged();
    }

    // 自动化槽位方向与侧面配置约束
    private static final int[] SLOTS_NONE = new int[]{};
    private static final int[] SLOTS_ALL_INPUT = new int[]{SLOT_MAIN, SLOT_SECOND, SLOT_CATALYST};
    private static final int[] SLOTS_OUTPUT_ONLY = new int[]{SLOT_MAIN};

    public int getSideConfig(Direction side) {
        return sideConfigs[side.ordinal()];
    }

    public void cycleSideConfig(Direction side) {
        sideConfigs[side.ordinal()] = (sideConfigs[side.ordinal()] + 1) % 4;
        setChanged();
    }

    public boolean isAutoEject() {
        return autoEject;
    }

    public void toggleAutoEject() {
        this.autoEject = !this.autoEject;
        setChanged();
    }

    @Override
    public int[] getSlotsForFace(@NotNull Direction side) {
        int config = getSideConfig(side);
        return switch (config) {
            case 0 -> SLOTS_ALL_INPUT;    // INPUT
            case 1 -> SLOTS_OUTPUT_ONLY;  // OUTPUT
            default -> SLOTS_NONE;        // ENERGY_ONLY 或 DISABLED
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, @Nullable Direction side) {
        if (side != null && getSideConfig(side) != 0) {
            return false; // 非输入面禁止输入
        }
        if (slot == SLOT_ANVIL || slot == SLOT_UPGRADE_INPUT || slot == SLOT_UPGRADE_OUTPUT) {
            return false; // 铁砧模具槽与升级管理槽禁止外部自动化管道乱塞
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction side) {
        if (getSideConfig(side) != 1) {
            return false; // 非输出面禁止抽取
        }
        return slot == SLOT_MAIN && ForgingCapability.get(stack) == null;
    }

    // ========== NBT 序列化与数据同步 ==========

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        energyStorage.deserializeNBT(tag.getCompound("EnergyStorage"));
        autoForgeEnabled = tag.getBoolean("AutoForgeEnabled");
        selectedRecipeId = tag.getString("SelectedRecipeId");
        recipeLocked = tag.getBoolean("RecipeLocked");
        lockedRecipeId = tag.getString("LockedRecipeId");
        autoEject = tag.contains("AutoEject") ? tag.getBoolean("AutoEject") : true;
        if (tag.contains("SideConfigs")) {
            int[] arr = tag.getIntArray("SideConfigs");
            if (arr.length == 6) {
                System.arraycopy(arr, 0, sideConfigs, 0, 6);
            }
        }
        autoForgeTimer = tag.getInt("AutoForgeTimer");
        currentState = tag.getInt("CurrentState");
        upgradeTicks = tag.getInt("UpgradeTicks");

        if (tag.contains("InstalledUpgrades")) {
            CompoundTag upgradesTag = tag.getCompound("InstalledUpgrades");
            installedUpgrades.clear();
            for (org.shengxi.common.upgrade.ForgeUpgradeType type : org.shengxi.common.upgrade.ForgeUpgradeType.values()) {
                if (upgradesTag.contains(type.getName())) {
                    int count = upgradesTag.getInt(type.getName());
                    if (count > 0) {
                        installedUpgrades.put(type, Math.min(count, type.getMaxCount()));
                    }
                }
            }
            recalculateEnergyStorage();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.put("EnergyStorage", energyStorage.serializeNBT());
        tag.putBoolean("AutoForgeEnabled", autoForgeEnabled);
        tag.putString("SelectedRecipeId", selectedRecipeId);
        tag.putBoolean("RecipeLocked", recipeLocked);
        tag.putString("LockedRecipeId", lockedRecipeId);
        tag.putBoolean("AutoEject", autoEject);
        tag.putIntArray("SideConfigs", sideConfigs);
        tag.putInt("AutoForgeTimer", autoForgeTimer);
        tag.putInt("CurrentState", currentState);
        tag.putInt("UpgradeTicks", upgradeTicks);

        CompoundTag upgradesTag = new CompoundTag();
        for (var entry : installedUpgrades.entrySet()) {
            upgradesTag.putInt(entry.getKey().getName(), entry.getValue());
        }
        tag.put("InstalledUpgrades", upgradesTag);
    }


    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ========== Capabilities ==========

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return itemHandlers[0].cast();
            }
            return switch (side) {
                case UP -> itemHandlers[0].cast();
                case DOWN -> itemHandlers[1].cast();
                default -> itemHandlers[2].cast();
            };
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
        for (var handler : itemHandlers) {
            handler.invalidate();
        }
    }

    // ========== MenuProvider ==========

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.mekatfc.electric_forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new ElectricForgeMenu(containerId, playerInventory, this, this.dataAccess);
    }
}
