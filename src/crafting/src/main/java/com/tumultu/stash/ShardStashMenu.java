package com.tumultu.stash;

import com.tumultu.shard.StashApplicable;
import com.tumultu.registry.CurrencyItems;
import com.tumultu.registry.CraftingMenus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * Only ONE currency is ever "in view" at a time - the 5x3 grid's 15 real {@code Slot}s always
 * belong to whichever currency is currently selected. Each currency's true total beyond what's
 * currently sitting in those 15 slots is tracked as a plain {@code int} counter
 * ({@link ShardStashBlockEntity#getHiddenOverflow}) - there's no cap on it, so there's no need for
 * pagination: {@link #broadcastChanges()} tops the visible cells back up towards a normal full
 * stack from the counter whenever one is empty or partial, so the grid always shows "the front
 * of" the total. It never sweeps a cell back into the counter just for being full - nothing else
 * ever deposits into these cells directly, so a full stack sitting in view is just a normal,
 * stable state. Selecting a different currency (clicking its row in the button list) sweeps
 * whatever's currently visible into the *old* currency's counter (a one-time move, not a
 * per-tick one), then unpacks the *new* currency's counter back into the now-empty cells.
 *
 * <p>Depositing a shard via shift-click routes straight into that item's own counter,
 * independent of whichever currency happens to be selected/viewed - so it always lands in its own
 * category, never wherever the grid is currently pointed. Every {@code ItemStack} that ever
 * exists is a completely normal one (never above its own real max stack size), and the backing
 * container itself only ever has 16 real slots (15 view + 1 target) - small enough that vanilla's
 * own byte-indexed NBT persistence is safe again, unlike the large-flat-array version this
 * replaced.
 *
 * <p>Since the client only receives slot-sync data for whichever currency is currently in view,
 * each currency's total is tracked separately via a {@link DataSlot} (recomputed every tick),
 * the same built-in sync mechanism furnaces use for burn time.
 */
public class ShardStashMenu extends AbstractContainerMenu {
    public static final int CURRENCY_COUNT = 15;
    private static final int VIEW_SIZE = 15;
    private static final int SLOT_TARGET = VIEW_SIZE;
    public static final int BACKING_CONTAINER_SIZE = VIEW_SIZE + 1;
    public static final int APPLY_BUTTON = 1_000_000;

    // default value when opening stash
    private static final int NO_SELECTION = -1;
    public static final List<Supplier<Item>> CURRENCIES = List.of(
            CurrencyItems.SHAPING_SHARD, CurrencyItems.GROWTH_SHARD,
            CurrencyItems.HEIGHTENING_SHARD, CurrencyItems.INFUSION_SHARD,
            CurrencyItems.CLEANSING_SHARD, CurrencyItems.TWISTING_SHARD,
            CurrencyItems.PEAKING_SHARD, CurrencyItems.WEAVING_SHARD,
            CurrencyItems.SEVERING_SHARD, CurrencyItems.ENDFUSING_SHARD,
            CurrencyItems.UNSPOKEN_PROMISE, CurrencyItems.REBINDING_ASHES,
            CurrencyItems.EMPOWERING_GEM, CurrencyItems.IMBUING_SHARD,
            CurrencyItems.MYSTERIOUS_BOOKPAGE
    );

    private final Container container;
    private final DataSlot[] currencyTotalSlots = new DataSlot[CURRENCY_COUNT];
    private DataSlot selectedCurrencySlot;
    private int selectedCurrency = NO_SELECTION;
    public ShardStashMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(BACKING_CONTAINER_SIZE));
    }

    public ShardStashMenu(int containerId, Inventory playerInventory, Container container) {
        super(CraftingMenus.SHARD_STASH_MENU.get(), containerId);
        checkContainerSize(container, BACKING_CONTAINER_SIZE);
        this.container = container;
        container.startOpen(playerInventory.player);

        // Always open with nothing selected, regardless of what was showing when this was last
        // closed - resuming a stale selection risks the player not noticing what's still loaded
        // in the grid and hitting Apply against the wrong currency. Whatever was physically
        // sitting in the view slots (from before closing) gets swept back into its owner's
        // counter first, so nothing is lost - it just isn't shown until explicitly reselected.
        if (container instanceof ShardStashBlockEntity blockEntity) {
            int previouslySelected = blockEntity.getSelectedCurrency();
            if (previouslySelected != NO_SELECTION) {
                int overflow = blockEntity.getHiddenOverflow(previouslySelected);
                for (int cell = 0; cell < VIEW_SIZE; cell++) {
                    ItemStack stack = container.getItem(cell);
                    if (!stack.isEmpty()) {
                        overflow += stack.getCount();
                        container.setItem(cell, ItemStack.EMPTY);
                    }
                }
                blockEntity.setHiddenOverflow(previouslySelected, overflow);
            }
            blockEntity.setSelectedCurrency(NO_SELECTION);
        }

        // 5x3 grid, kept in sync with ShardStashScreen's layout constants. Always shows whichever
        // currency is currently selected - see this class's own doc comment.
        int columns = 5;
        for (int cell = 0; cell < VIEW_SIZE; cell++) {
            int col = cell % columns;
            int row = cell / columns;
            int x = 8 + col * 18;
            int y = 18 + row * 18;
            this.addSlot(new Slot(container, cell, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return selectedCurrency != NO_SELECTION && stack.getItem() == CURRENCIES.get(selectedCurrency).get();
                }
            });
        }

        this.addSlot(new Slot(container, SLOT_TARGET, 140, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        this.addStandardInventorySlots(playerInventory, 8, 218);

        for (int i = 0; i < CURRENCY_COUNT; i++) {
            currencyTotalSlots[i] = DataSlot.standalone();
            this.addDataSlot(currencyTotalSlots[i]);
        }
        selectedCurrencySlot = DataSlot.standalone();
        selectedCurrencySlot.set(this.selectedCurrency);
        this.addDataSlot(selectedCurrencySlot);
    }

    public ItemStack getTargetStack() {
        return container.getItem(SLOT_TARGET);
    }
    public int getCurrencyTotal(int currencyIndex) {
        return currencyTotalSlots[currencyIndex].get();
    }
    public int getSelectedCurrency() {
        return selectedCurrencySlot.get();
    }

    @Override
    public void broadcastChanges() {
        if (container instanceof ShardStashBlockEntity blockEntity) {
            normalizeSelectedCurrencyView(blockEntity);
            for (int currency = 0; currency < CURRENCY_COUNT; currency++) {
                int total = blockEntity.getHiddenOverflow(currency);
                if (currency == selectedCurrency) {
                    for (int cell = 0; cell < VIEW_SIZE; cell++) {
                        total += container.getItem(cell).getCount();
                    }
                }
                currencyTotalSlots[currency].set(total);
            }
            selectedCurrencySlot.set(selectedCurrency);
        }
        super.broadcastChanges();
    }

    /** Keeps the visible grid showing "the front of" the selected currency's total: sweeps a cell
     * up from the counter (whether it's completely empty or just partially filled - e.g. after the
     * player withdrew some by hand) - it never removes anything from a visible cell. A cell
     * sitting at a normal full stack is just a normal, stable state; nothing else ever deposits
     * into these cells directly (shift-click deposits go straight into the counter - see
     * {@link #quickMoveStack}), so there's never a need to sweep one away to "make room." An
     * earlier version swept full cells into the counter every tick, which then got immediately
     * refilled from that same counter the very next tick - a permanent full/empty oscillation
     * that showed up as visible flicker. */
    private void normalizeSelectedCurrencyView(ShardStashBlockEntity blockEntity) {
        if (selectedCurrency == NO_SELECTION) {
            return;
        }
        int overflow = blockEntity.getHiddenOverflow(selectedCurrency);
        if (overflow <= 0) {
            return;
        }
        Item item = CURRENCIES.get(selectedCurrency).get();
        int maxStack = item.getDefaultInstance().getMaxStackSize();

        for (int cell = 0; cell < VIEW_SIZE && overflow > 0; cell++) {
            ItemStack stack = container.getItem(cell);
            if (stack.isEmpty()) {
                int amount = Math.min(maxStack, overflow);
                overflow -= amount;
                container.setItem(cell, new ItemStack(item, amount));
            } else if (stack.getItem() == item && stack.getCount() < maxStack) {
                int amount = Math.min(maxStack - stack.getCount(), overflow);
                stack.grow(amount);
                overflow -= amount;
            }
        }
        blockEntity.setHiddenOverflow(selectedCurrency, overflow);
    }

    /** Sweeps whatever's currently visible into the outgoing currency's own counter (so nothing
     * is lost switching away from it), then selects the new one - its own counter gets unpacked
     * back into the now-empty cells on the next {@link #broadcastChanges()} tick. */
    private void selectCurrency(int newCurrency, ShardStashBlockEntity blockEntity) {
        if (selectedCurrency != NO_SELECTION) {
            int outgoingOverflow = blockEntity.getHiddenOverflow(selectedCurrency);
            for (int cell = 0; cell < VIEW_SIZE; cell++) {
                ItemStack stack = container.getItem(cell);
                if (!stack.isEmpty()) {
                    outgoingOverflow += stack.getCount();
                    container.setItem(cell, ItemStack.EMPTY);
                }
            }
            blockEntity.setHiddenOverflow(selectedCurrency, outgoingOverflow);
        }

        this.selectedCurrency = newCurrency;
        blockEntity.setSelectedCurrency(newCurrency);
        normalizeSelectedCurrencyView(blockEntity);
        this.broadcastFullState();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(container instanceof ShardStashBlockEntity blockEntity)) {
            return false;
        }
        if (buttonId >= 0 && buttonId < CURRENCY_COUNT) {
            selectCurrency(buttonId, blockEntity);
            return true;
        }
        if (buttonId == APPLY_BUTTON) {
            return applySelectedCurrency(player, blockEntity);
        }
        return false;
    }

    /** Consumes from whatever's currently visible first, falling back to the hidden counter
     * directly (decrementing it by 1) if the view is empty but there's still overflow - so
     * applying never requires manually pulling a stack into view first. */
    private boolean applySelectedCurrency(Player player, ShardStashBlockEntity blockEntity) {
        if (selectedCurrency == NO_SELECTION) {
            return false;
        }
        ItemStack targetStack = getTargetStack();
        if (targetStack.isEmpty()) {
            return false;
        }

        for (int cell = 0; cell < VIEW_SIZE; cell++) {
            ItemStack candidate = container.getItem(cell);
            if (!candidate.isEmpty()) {
                if (!(candidate.getItem() instanceof StashApplicable applicable)) {
                    return false;
                }
                if (!applicable.tryApply((ServerLevel) player.level(), player, targetStack)) {
                    return false;
                }
                candidate.shrink(1);
                return true;
            }
        }

        int overflow = blockEntity.getHiddenOverflow(selectedCurrency);
        if (overflow <= 0) {
            return false;
        }
        if (!(CURRENCIES.get(selectedCurrency).get() instanceof StashApplicable applicable)) {
            return false;
        }
        if (!applicable.tryApply((ServerLevel) player.level(), player, targetStack)) {
            return false;
        }
        blockEntity.setHiddenOverflow(selectedCurrency, overflow - 1);
        return true;
    }

    private static int indexOfCurrency(Item item) {
        for (int i = 0; i < CURRENCY_COUNT; i++) {
            if (CURRENCIES.get(i).get() == item) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (index < BACKING_CONTAINER_SIZE) {
            if (!this.moveItemStackTo(stackInSlot, BACKING_CONTAINER_SIZE, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int currencyIndex = indexOfCurrency(stackInSlot.getItem());
            if (currencyIndex >= 0 && container instanceof ShardStashBlockEntity blockEntity) {
                // Routes straight into that currency's own counter - independent of whichever
                // currency is currently selected/viewed, so it always lands in its own category.
                int newTotal = blockEntity.getHiddenOverflow(currencyIndex) + stackInSlot.getCount();
                blockEntity.setHiddenOverflow(currencyIndex, newTotal);
                stackInSlot.shrink(stackInSlot.getCount());
            } else if (!this.slots.get(SLOT_TARGET).mayPlace(stackInSlot)
                    || !this.moveItemStackTo(stackInSlot, SLOT_TARGET, SLOT_TARGET + 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stackInSlot);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
