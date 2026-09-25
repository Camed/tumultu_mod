package com.tumultu.bench;

import com.tumultu.registry.CraftingBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// entity holds items stored in slots
public class CraftingBenchBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_COUNT = 4;
    public static final int SLOT_TARGET = 0;
    public static final int SLOT_RECIPE = 1;
    public static final int SLOT_PAYMENT = 2;
    public static final int SLOT_LOCK = 3;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public CraftingBenchBlockEntity(BlockPos pos, BlockState state) {
        super(CraftingBlockEntities.CRAFTING_BENCH.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.tumultu.crafting_bench");
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CraftingBenchMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }
}
