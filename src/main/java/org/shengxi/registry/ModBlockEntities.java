package org.shengxi.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.shengxi.Mekatfc;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;

/**
 * MekaTFC 方块实体注册表 (NeoForge 1.21.1)
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Mekatfc.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricForgeBlockEntity>> ELECTRIC_FORGE =
            BLOCK_ENTITIES.register("electric_forge", () ->
                    BlockEntityType.Builder.of(ElectricForgeBlockEntity::new, ModBlocks.ELECTRIC_FORGE.get())
                            .build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
