package com.tumultu.stash;

import com.tumultu.registry.CraftingBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Backs {@link ShardStashMenu}: just 16 real slots (15 view + 1 target - small enough that
 * vanilla's own byte-indexed {@link ContainerHelper} persistence is safe), plus a per-currency
 * "hidden overflow" counter with no cap - see {@link ShardStashMenu}'s own doc comment for how
 * the two combine to make the grid always show "the front of" each currency's true total. */
public class ShardStashBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_COUNT = ShardStashMenu.BACKING_CONTAINER_SIZE;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] hiddenOverflow = new int[ShardStashMenu.CURRENCY_COUNT];
    private int selectedCurrency = 0;

    public ShardStashBlockEntity(BlockPos pos, BlockState state) {
        super(CraftingBlockEntities.SHARD_STASH.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.tumultu.shard_stash");
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
        return new ShardStashMenu(containerId, inventory, this);
    }

    public int getHiddenOverflow(int currencyIndex) {
        return hiddenOverflow[currencyIndex];
    }

    public void setHiddenOverflow(int currencyIndex, int value) {
        hiddenOverflow[currencyIndex] = value;
    }

    public int getSelectedCurrency() {
        return selectedCurrency;
    }

    public void setSelectedCurrency(int value) {
        selectedCurrency = value;
    }

    /**
     * The default {@code BlockEntity.preRemoveSideEffects} already drops this block entity's own
     * container contents on break (since it {@code instanceof Container}) - that's the 15 view
     * slots + target for whichever currency happened to be selected. Everything else - every
     * other currency's hidden overflow, and the rest of the selected one's total beyond what fit
     * in the 15 visible cells - lives only as plain ints and would otherwise vanish silently.
     * This drops all of that too, split into properly-capped stacks per currency.
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null) {
            return;
        }
        for (int i = 0; i < hiddenOverflow.length; i++) {
            int remaining = hiddenOverflow[i];
            if (remaining <= 0) {
                continue;
            }
            Item item = ShardStashMenu.CURRENCIES.get(i).get();
            int maxStack = item.getDefaultInstance().getMaxStackSize();
            while (remaining > 0) {
                int amount = Math.min(maxStack, remaining);
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(item, amount));
                remaining -= amount;
            }
            hiddenOverflow[i] = 0;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        input.getIntArray("hidden_overflow").ifPresent(saved -> {
            int count = Math.min(saved.length, hiddenOverflow.length);
            System.arraycopy(saved, 0, hiddenOverflow, 0, count);
        });
        selectedCurrency = input.getIntOr("selected_currency", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putIntArray("hidden_overflow", hiddenOverflow);
        output.putInt("selected_currency", selectedCurrency);
    }
}
