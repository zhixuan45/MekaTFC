package org.shengxi.common.network;

import net.dries007.tfc.common.component.forge.ForgeStep;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.shengxi.Mekatfc;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;
import org.shengxi.common.upgrade.ForgeUpgradeType;

/**
 * MekaTFC 网络通信 Payload 管理器 (NeoForge 1.21.1)
 */
public class ModPackets {

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Mekatfc.MODID).versioned("1");

        registrar.playToServer(ForgeStepActionPayload.TYPE, ForgeStepActionPayload.STREAM_CODEC, ForgeStepActionPayload::handle);
        registrar.playToServer(ToggleAutoForgePayload.TYPE, ToggleAutoForgePayload.STREAM_CODEC, ToggleAutoForgePayload::handle);
        registrar.playToServer(SelectRecipePlanPayload.TYPE, SelectRecipePlanPayload.STREAM_CODEC, SelectRecipePlanPayload::handle);
        registrar.playToServer(ToggleRecipeLockPayload.TYPE, ToggleRecipeLockPayload.STREAM_CODEC, ToggleRecipeLockPayload::handle);
        registrar.playToServer(UpdateSideConfigPayload.TYPE, UpdateSideConfigPayload.STREAM_CODEC, UpdateSideConfigPayload::handle);
        registrar.playToServer(UninstallUpgradePayload.TYPE, UninstallUpgradePayload.STREAM_CODEC, UninstallUpgradePayload::handle);
        registrar.playToServer(WeldActionPayload.TYPE, WeldActionPayload.STREAM_CODEC, WeldActionPayload::handle);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    /**
     * 7. 手动焊接请求 Payload
     */
    public record WeldActionPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<WeldActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Mekatfc.MODID, "weld_action"));

        public static final StreamCodec<FriendlyByteBuf, WeldActionPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, WeldActionPayload::pos,
                WeldActionPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(WeldActionPayload payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.level().isLoaded(payload.pos())) {
                    BlockEntity be = serverPlayer.level().getBlockEntity(payload.pos());
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        forgeEntity.performWeld();
                    }
                }
            });
        }
    }

    /**
     * 1. 手动击打请求 Payload
     */
    public record ForgeStepActionPayload(BlockPos pos, int stepOrdinal) implements CustomPacketPayload {
        public static final Type<ForgeStepActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Mekatfc.MODID, "forge_step_action"));

        public static final StreamCodec<FriendlyByteBuf, ForgeStepActionPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, ForgeStepActionPayload::pos,
                ByteBufCodecs.VAR_INT, ForgeStepActionPayload::stepOrdinal,
                ForgeStepActionPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ForgeStepActionPayload payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.level().isLoaded(payload.pos())) {
                    BlockEntity be = serverPlayer.level().getBlockEntity(payload.pos());
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        ForgeStep[] steps = ForgeStep.values();
                        if (payload.stepOrdinal() >= 0 && payload.stepOrdinal() < steps.length) {
                            forgeEntity.performManualStep(steps[payload.stepOrdinal()]);
                        }
                    }
                }
            });
        }
    }

    /**
     * 2. 切换自动锻造开关 Payload
     */
    public record ToggleAutoForgePayload(BlockPos pos, boolean enabled) implements CustomPacketPayload {
        public static final Type<ToggleAutoForgePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Mekatfc.MODID, "toggle_auto_forge"));

        public static final StreamCodec<FriendlyByteBuf, ToggleAutoForgePayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, ToggleAutoForgePayload::pos,
                ByteBufCodecs.BOOL, ToggleAutoForgePayload::enabled,
                ToggleAutoForgePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ToggleAutoForgePayload payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.level().isLoaded(payload.pos())) {
                    BlockEntity be = serverPlayer.level().getBlockEntity(payload.pos());
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        forgeEntity.setAutoForgeEnabled(payload.enabled());
                    }
                }
            });
        }
    }

    /**
     * 3. 选择配方图纸 Payload
     */
    public record SelectRecipePlanPayload(BlockPos pos, String recipeId) implements CustomPacketPayload {
        public static final Type<SelectRecipePlanPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Mekatfc.MODID, "select_recipe_plan"));

        public static final StreamCodec<FriendlyByteBuf, SelectRecipePlanPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, SelectRecipePlanPayload::pos,
                ByteBufCodecs.STRING_UTF8, SelectRecipePlanPayload::recipeId,
                SelectRecipePlanPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(SelectRecipePlanPayload payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.level().isLoaded(payload.pos())) {
                    BlockEntity be = serverPlayer.level().getBlockEntity(payload.pos());
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        forgeEntity.setSelectedRecipe(payload.recipeId());
                        ResourceLocation resId = ResourceLocation.tryParse(payload.recipeId());
                        if (resId != null) {
                            var opt = serverPlayer.level().getRecipeManager().byKey(resId);
                            if (opt.isPresent() && opt.get().value() instanceof net.dries007.tfc.common.recipes.AnvilRecipe) {
                                forgeEntity.chooseRecipe((net.minecraft.world.item.crafting.RecipeHolder<net.dries007.tfc.common.recipes.AnvilRecipe>) (net.minecraft.world.item.crafting.RecipeHolder<?>) opt.get());
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 4. 切换配方锁定状态 Payload
     */
    public record ToggleRecipeLockPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<ToggleRecipeLockPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Mekatfc.MODID, "toggle_recipe_lock"));

        public static final StreamCodec<FriendlyByteBuf, ToggleRecipeLockPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, ToggleRecipeLockPayload::pos,
                ToggleRecipeLockPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ToggleRecipeLockPayload payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.level().isLoaded(payload.pos())) {
                    BlockEntity be = serverPlayer.level().getBlockEntity(payload.pos());
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        forgeEntity.toggleRecipeLock();
                    }
                }
            });
        }
    }

    /**
     * 5. 更新侧面配置 Payload
     */
    public record UpdateSideConfigPayload(BlockPos pos, int sideOrdinal, boolean isAutoEjectToggle) implements CustomPacketPayload {
        public static final Type<UpdateSideConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Mekatfc.MODID, "update_side_config"));

        public static final StreamCodec<FriendlyByteBuf, UpdateSideConfigPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, UpdateSideConfigPayload::pos,
                ByteBufCodecs.VAR_INT, UpdateSideConfigPayload::sideOrdinal,
                ByteBufCodecs.BOOL, UpdateSideConfigPayload::isAutoEjectToggle,
                UpdateSideConfigPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(UpdateSideConfigPayload payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.level().isLoaded(payload.pos())) {
                    BlockEntity be = serverPlayer.level().getBlockEntity(payload.pos());
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        if (payload.isAutoEjectToggle()) {
                            forgeEntity.toggleAutoEject();
                        } else if (payload.sideOrdinal() >= 0 && payload.sideOrdinal() < 6) {
                            forgeEntity.cycleSideConfig(Direction.values()[payload.sideOrdinal()]);
                        }
                    }
                }
            });
        }
    }

    /**
     * 6. 升级卸载请求 Payload
     */
    public record UninstallUpgradePayload(BlockPos pos, int upgradeOrdinal, boolean removeAll) implements CustomPacketPayload {
        public static final Type<UninstallUpgradePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Mekatfc.MODID, "uninstall_upgrade"));

        public static final StreamCodec<FriendlyByteBuf, UninstallUpgradePayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, UninstallUpgradePayload::pos,
                ByteBufCodecs.VAR_INT, UninstallUpgradePayload::upgradeOrdinal,
                ByteBufCodecs.BOOL, UninstallUpgradePayload::removeAll,
                UninstallUpgradePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(UninstallUpgradePayload payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.level().isLoaded(payload.pos())) {
                    BlockEntity be = serverPlayer.level().getBlockEntity(payload.pos());
                    if (be instanceof ElectricForgeBlockEntity forgeEntity) {
                        ForgeUpgradeType[] types = ForgeUpgradeType.values();
                        if (payload.upgradeOrdinal() >= 0 && payload.upgradeOrdinal() < types.length) {
                            forgeEntity.uninstallUpgrade(types[payload.upgradeOrdinal()], payload.removeAll());
                        }
                    }
                }
            });
        }
    }
}
