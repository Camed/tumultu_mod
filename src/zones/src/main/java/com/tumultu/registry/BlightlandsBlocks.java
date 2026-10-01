package com.tumultu.registry;

import com.tumultu.TumultuMod;
import com.tumultu.blightidol.BlightIdolBlock;
import com.tumultu.blocks.FlammableBlock;
import com.tumultu.blocks.FlammableRotatedBlightedPillarBlock;
import com.tumultu.blocks.FlammableTintedLeavesBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Optional;

// blocks associated with blightlands biome
public class BlightlandsBlocks {
    // No BlockItem: worldgen-only prop, never obtainable or player-placeable (see BlightIdolBlock).
    public static final DeferredBlock<BlightIdolBlock> BLIGHT_IDOL = TumultuBlocksRegistry.BLOCKS.register(
            "blight_idol",
            registryName -> new BlightIdolBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(5.0F)
                    .requiresCorrectToolForDrops()
                    .noLootTable())
    );

    public static final DeferredBlock<FlammableRotatedBlightedPillarBlock> STRIPPED_BLIGHTED_LOG = TumultuBlocksRegistry.BLOCKS.register(
            "stripped_blighted_log",
            registryName -> new FlammableRotatedBlightedPillarBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG).setId(ResourceKey.create(Registries.BLOCK, registryName)))
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_BLIGHTED_LOG_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(STRIPPED_BLIGHTED_LOG);

    public static final DeferredBlock<FlammableRotatedBlightedPillarBlock> STRIPPED_BLIGHTED_WOOD = TumultuBlocksRegistry.BLOCKS.register(
            "stripped_blighted_wood",
            registryName -> new FlammableRotatedBlightedPillarBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD).setId(ResourceKey.create(Registries.BLOCK, registryName)))
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_BLIGHTED_WOOD_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(STRIPPED_BLIGHTED_WOOD);

    public static final DeferredBlock<FlammableRotatedBlightedPillarBlock> BLIGHTED_LOG = TumultuBlocksRegistry.BLOCKS.register(
            "blighted_log",
            registryName -> new FlammableRotatedBlightedPillarBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).setId(ResourceKey.create(Registries.BLOCK, registryName)),
                    () -> STRIPPED_BLIGHTED_LOG.get())
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLIGHTED_LOG_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(BLIGHTED_LOG);

    public static final DeferredBlock<FlammableRotatedBlightedPillarBlock> BLIGHTED_WOOD = TumultuBlocksRegistry.BLOCKS.register(
            "blighted_wood",
            registryName -> new FlammableRotatedBlightedPillarBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).setId(ResourceKey.create(Registries.BLOCK, registryName)),
                    () -> STRIPPED_BLIGHTED_WOOD.get())
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLIGHTED_WOOD_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(BLIGHTED_WOOD);

    public static final DeferredBlock<FlammableBlock> BLIGHTED_PLANKS = TumultuBlocksRegistry.BLOCKS.register(
            "blighted_planks",
            registryName -> new FlammableBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(ResourceKey.create(Registries.BLOCK, registryName)),
                    5, 20)
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLIGHTED_PLANKS_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(BLIGHTED_PLANKS);

    public static final DeferredBlock<FlammableTintedLeavesBlock> BLIGHTED_LEAVES = TumultuBlocksRegistry.BLOCKS.register(
            "blighted_leaves",
            registryName -> new FlammableTintedLeavesBlock(0.01F,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).setId(ResourceKey.create(Registries.BLOCK, registryName)),
                    30, 60)
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLIGHTED_LEAVES_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(BLIGHTED_LEAVES);

    private static final ResourceKey<ConfiguredFeature<?, ?>> BLIGHTED_TREE_FEATURE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "blighted_tree"));

    private static final TreeGrower BLIGHTED_TREE_GROWER =
            new TreeGrower("blighted", Optional.empty(), Optional.of(BLIGHTED_TREE_FEATURE), Optional.empty());

    public static final DeferredBlock<SaplingBlock> BLIGHTED_SAPLING = TumultuBlocksRegistry.BLOCKS.register(
            "blighted_sapling",
            registryName -> new SaplingBlock(BLIGHTED_TREE_GROWER,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).setId(ResourceKey.create(Registries.BLOCK, registryName)))
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLIGHTED_SAPLING_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(BLIGHTED_SAPLING);

    public static final DeferredBlock<DoorBlock> BLIGHTED_DOOR = TumultuBlocksRegistry.BLOCKS.register(
            "blighted_door",
            registryName -> new DoorBlock(BlightlandsWoodTypes.BLIGHTED_SET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR).setId(ResourceKey.create(Registries.BLOCK, registryName)))
    );

    public static final DeferredItem<Item> BLIGHTED_DOOR_ITEM = TumultuItemsRegistry.ITEMS.register(
            "blighted_door",
            registryName -> new DoubleHighBlockItem(BLIGHTED_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName)).stacksTo(64))
    );

    public static final DeferredBlock<StandingSignBlock> BLIGHTED_SIGN = TumultuBlocksRegistry.BLOCKS.register(
            "blighted_sign",
            registryName -> new StandingSignBlock(BlightlandsWoodTypes.BLIGHTED,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN).setId(ResourceKey.create(Registries.BLOCK, registryName)))
    );

    public static final DeferredBlock<WallSignBlock> BLIGHTED_WALL_SIGN = TumultuBlocksRegistry.BLOCKS.register(
            "blighted_wall_sign",
            registryName -> new WallSignBlock(BlightlandsWoodTypes.BLIGHTED,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN).setId(ResourceKey.create(Registries.BLOCK, registryName)))
    );

    public static final DeferredItem<Item> BLIGHTED_SIGN_ITEM = TumultuItemsRegistry.ITEMS.register(
            "blighted_sign",
            registryName -> new SignItem(BLIGHTED_SIGN.get(), BLIGHTED_WALL_SIGN.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName)).stacksTo(16))
    );

    public static final DeferredItem<Item> BLIGHTED_BOAT_ITEM = TumultuItemsRegistry.ITEMS.register(
            "blighted_boat",
            registryName -> new BoatItem(BlightlandsEntityTypes.BLIGHTED_BOAT.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName)).stacksTo(1))
    );

    public static final DeferredItem<Item> BLIGHTED_CHEST_BOAT_ITEM = TumultuItemsRegistry.ITEMS.register(
            "blighted_chest_boat",
            registryName -> new BoatItem(BlightlandsEntityTypes.BLIGHTED_CHEST_BOAT.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName)).stacksTo(1))
    );

    public static void onAddBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN, BLIGHTED_SIGN.get(), BLIGHTED_WALL_SIGN.get());
    }
}
