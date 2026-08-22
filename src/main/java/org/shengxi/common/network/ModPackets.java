package org.shengxi.common.network;

import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.shengxi.Mekatfc;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;

import java.util.function.Supplier;

/**
 * MekaTFC 网络通信包管理器
 */
public class ModPackets {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mekatfc.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        // 注册手动击打包
        INSTANCE.messageBuilder(ForgeStepActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ForgeStepActionPacket::new)
                .encoder(ForgeStepActionPacket::encode)
                .consumerMainThread(ForgeStepActionPacket::handle)
                .add();

        // 注册自动锻造开关包
        INSTANCE.messageBuilder(ToggleAutoForgePacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ToggleAutoForgePacket::new)
                .encoder(ToggleAutoForgePacket::encode)
                .consumerMainThread(ToggleAutoForgePacket::handle)
                .add();

        // 注册选择图纸包
        INSTANCE.messageBuilder(SelectRecipePlanPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SelectRecipePlanPacket::new)
                .encoder(SelectRecipePlanPacket::encode)
                .consumerMainThread(SelectRecipePlanPacket::handle)
                .add();

        // 注册切换配方锁定包
        INSTANCE.messageBuilder(ToggleRecipeLockPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ToggleRecipeLockPacket::new)
                .encoder(ToggleRecipeLockPacket::encode)
                .consumerMainThread(ToggleRecipeLockPacket::handle)
                .add();

        // 注册更新侧面配置包
        INSTANCE.messageBuilder(UpdateSideConfigPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateSideConfigPacket::new)
                .encoder(UpdateSideConfigPacket::encode)
                .consumerMainThread(UpdateSideConfigPacket::handle)
                .add();

        // 注册升级卸载包
        INSTANCE.messageBuilder(UninstallUpgradePacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UninstallUpgradePacket::new)
                .encoder(UninstallUpgradePacket::encode)
                .consumerMainThread(UninstallUpgradePacket::handle)
                .add();
    }


    /**
     * 手动击打请求包
     */
    public static class ForgeStepActionPacket {
        private final BlockPos pos;
        private final int stepOrdinal;

        public ForgeStepActionPacket(BlockPos pos, int stepOrdinal) {
            this.pos = pos;
            this.stepOrdinal = stepOrdinal;
        }

        public ForgeStepActionPacket(FriendlyByteBuf buf) {
            this.pos = buf.readBlockPos();
            this.stepOrdinal = buf.readVarInt();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeVarInt(stepOrdinal);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level().isLoaded(pos)) {
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        ForgeStep[] steps = ForgeStep.values();
                        if (stepOrdinal >= 0 && stepOrdinal < steps.length) {
                            forgeEntity.performManualStep(steps[stepOrdinal]);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * 切换自动锻造开关请求包
     */
    public static class ToggleAutoForgePacket {
        private final BlockPos pos;
        private final boolean enabled;

        public ToggleAutoForgePacket(BlockPos pos, boolean enabled) {
            this.pos = pos;
            this.enabled = enabled;
        }

        public ToggleAutoForgePacket(FriendlyByteBuf buf) {
            this.pos = buf.readBlockPos();
            this.enabled = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeBoolean(enabled);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level().isLoaded(pos)) {
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        forgeEntity.setAutoForgeEnabled(enabled);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * 选择配方图纸请求包
     */
    public static class SelectRecipePlanPacket {
        private final BlockPos pos;
        private final String recipeId;

        public SelectRecipePlanPacket(BlockPos pos, String recipeId) {
            this.pos = pos;
            this.recipeId = recipeId != null ? recipeId : "";
        }

        public SelectRecipePlanPacket(FriendlyByteBuf buf) {
            this.pos = buf.readBlockPos();
            this.recipeId = buf.readUtf();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeUtf(recipeId);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level().isLoaded(pos)) {
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        forgeEntity.setSelectedRecipe(recipeId);
                        ResourceLocation resId = ResourceLocation.tryParse(recipeId);
                        if (resId != null) {
                            var opt = player.level().getRecipeManager().byKey(resId);
                            if (opt.isPresent() && opt.get() instanceof net.dries007.tfc.common.recipes.AnvilRecipe anvilRecipe) {
                                forgeEntity.chooseRecipe(anvilRecipe);
                            }
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * 切换配方锁定状态网络包
     */
    public static class ToggleRecipeLockPacket {
        private final BlockPos pos;

        public ToggleRecipeLockPacket(BlockPos pos) {
            this.pos = pos;
        }

        public ToggleRecipeLockPacket(FriendlyByteBuf buf) {
            this.pos = buf.readBlockPos();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level().isLoaded(pos)) {
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        forgeEntity.toggleRecipeLock();
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * 更新侧面配置与自动弹出网络包
     */
    public static class UpdateSideConfigPacket {
        private final BlockPos pos;
        private final int sideOrdinal;
        private final boolean isAutoEjectToggle;

        public UpdateSideConfigPacket(BlockPos pos, int sideOrdinal, boolean isAutoEjectToggle) {
            this.pos = pos;
            this.sideOrdinal = sideOrdinal;
            this.isAutoEjectToggle = isAutoEjectToggle;
        }

        public UpdateSideConfigPacket(FriendlyByteBuf buf) {
            this.pos = buf.readBlockPos();
            this.sideOrdinal = buf.readInt();
            this.isAutoEjectToggle = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeInt(sideOrdinal);
            buf.writeBoolean(isAutoEjectToggle);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level().isLoaded(pos)) {
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        if (isAutoEjectToggle) {
                            forgeEntity.toggleAutoEject();
                        } else if (sideOrdinal >= 0 && sideOrdinal < 6) {
                            forgeEntity.cycleSideConfig(net.minecraft.core.Direction.values()[sideOrdinal]);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * 升级卸载请求包
     */
    public static class UninstallUpgradePacket {
        private final BlockPos pos;
        private final int upgradeOrdinal;
        private final boolean removeAll;

        public UninstallUpgradePacket(BlockPos pos, int upgradeOrdinal, boolean removeAll) {
            this.pos = pos;
            this.upgradeOrdinal = upgradeOrdinal;
            this.removeAll = removeAll;
        }

        public UninstallUpgradePacket(FriendlyByteBuf buf) {
            this.pos = buf.readBlockPos();
            this.upgradeOrdinal = buf.readVarInt();
            this.removeAll = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeVarInt(upgradeOrdinal);
            buf.writeBoolean(removeAll);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level().isLoaded(pos)) {
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        org.shengxi.common.upgrade.ForgeUpgradeType[] types = org.shengxi.common.upgrade.ForgeUpgradeType.values();
                        if (upgradeOrdinal >= 0 && upgradeOrdinal < types.length) {
                            forgeEntity.uninstallUpgrade(types[upgradeOrdinal], removeAll);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}

