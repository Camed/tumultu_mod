package com.tumultu.bench;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixEffectApplier;
import com.tumultu.affix.BenchCrafting;
import com.tumultu.affix.PaymentTier;
import com.tumultu.affix.RecipeCategory;
import com.tumultu.affix.RolledAffix;
import com.tumultu.network.PreviewResultPayload;
import com.tumultu.registry.CurrencyItems;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.CraftingMenus;
import com.tumultu.registry.TumultuRecipeItems;
import com.tumultu.registry.TumultuTags;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class CraftingBenchMenu extends AbstractContainerMenu {
    public static final int SLOT_TARGET = 0;
    public static final int SLOT_RECIPE = 1;
    public static final int SLOT_PAYMENT = 2;
    public static final int SLOT_LOCK = 3;
    private static final int SLOT_COUNT = 4;

    // Sentinel button ids for resolving an Unspoken Promise preview - well outside the range of
    // legitimate candidate-affix indices, so they can never collide with one.
    public static final int ACCEPT_PREVIEW_BUTTON = 1_000_000;
    public static final int REJECT_PREVIEW_BUTTON = 1_000_001;

    private final Container container;

    // Set when the lock slot holds an Unspoken Promise and a candidate has been rolled but not
    // yet accepted/rejected.
    // todo: creating preview should consume unspoken promise
    private Optional<AffixData> pendingPreview = Optional.empty();

    public CraftingBenchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT));
    }

    public CraftingBenchMenu(int containerId, Inventory playerInventory, Container container) {
        super(CraftingMenus.CRAFTING_BENCH_MENU.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        this.container = container;
        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, SLOT_TARGET, 8, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TumultuTags.AFFIXABLE);
            }
        });
        this.addSlot(new Slot(container, SLOT_RECIPE, 56, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return TumultuRecipeItems.categoryOf(stack.getItem()).isPresent() || isCleansingShard(stack);
            }
        });
        this.addSlot(new Slot(container, SLOT_PAYMENT, 104, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PaymentTier.of(stack.getItem()).isPresent();
            }
        });
        this.addSlot(new Slot(container, SLOT_LOCK, 152, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isUnspokenPromise(stack);
            }
        });

        this.addStandardInventorySlots(playerInventory, 8, 130);
    }

    public ItemStack getTargetStack() {
        return container.getItem(SLOT_TARGET);
    }

    public ItemStack getRecipeStack() {
        return container.getItem(SLOT_RECIPE);
    }

    public ItemStack getLockStack() {
        return container.getItem(SLOT_LOCK);
    }

    public static boolean isCleansingShard(ItemStack stack) {
        return stack.getItem() == CurrencyItems.CLEANSING_SHARD.get();
    }

    public static boolean isUnspokenPromise(ItemStack stack) {
        return stack.getItem() == CurrencyItems.UNSPOKEN_PROMISE.get();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == ACCEPT_PREVIEW_BUTTON || buttonId == REJECT_PREVIEW_BUTTON) {
            return resolvePendingPreview(player, buttonId == ACCEPT_PREVIEW_BUTTON);
        }
        if (pendingPreview.isPresent()) {
            return false;
        }

        ItemStack targetStack = getTargetStack();
        ItemStack recipeStack = getRecipeStack();
        if (targetStack.isEmpty() || recipeStack.isEmpty()) {
            return false;
        }

        AffixData existing = targetStack.get(TumultuDataComponents.AFFIX_DATA.get());
        if (existing == null) {
            existing = AffixData.EMPTY;
        }

        RegistryAccess registryAccess = player.registryAccess();

        if (isCleansingShard(recipeStack)) {
            if (buttonId != 0) {
                return false;
            }
            Optional<AffixData> cleared = BenchCrafting.removeCraftedAffix(existing);
            if (cleared.isEmpty()) {
                return false;
            }

            targetStack.set(TumultuDataComponents.AFFIX_DATA.get(), cleared.get());
            AffixEffectApplier.apply(targetStack, cleared.get(), registryAccess, player.getRandom());
            recipeStack.shrink(1);
            return true;
        }

        ItemStack paymentStack = container.getItem(SLOT_PAYMENT);
        if (paymentStack.isEmpty()) {
            return false;
        }

        Optional<RecipeCategory> category = TumultuRecipeItems.categoryOf(recipeStack.getItem());
        Optional<PaymentTier> paymentTier = PaymentTier.of(paymentStack.getItem());
        if (category.isEmpty() || paymentTier.isEmpty()) {
            return false;
        }

        Optional<AffixData> crafted = BenchCrafting.craftAffix(
                registryAccess, targetStack, existing, category.get(), buttonId, paymentTier.get(), player.getRandom());
        if (crafted.isEmpty()) {
            return false;
        }

        ItemStack lockStack = getLockStack();
        if (isUnspokenPromise(lockStack)) {
            pendingPreview = crafted;
            List<RolledAffix> affixes = crafted.get().affixes();
            RolledAffix newAffix = affixes.get(affixes.size() - 1);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer,
                        new PreviewResultPayload(newAffix.affixId(), newAffix.tierIndex(), newAffix.rolledValue()));
            }
            return true;
        }

        targetStack.set(TumultuDataComponents.AFFIX_DATA.get(), crafted.get());
        AffixEffectApplier.apply(targetStack, crafted.get(), registryAccess, player.getRandom());

        recipeStack.shrink(1);
        paymentStack.shrink(1);
        return true;
    }


     // Unspoken Promise, the recipe item, and the payment ingot are all consumed together on
     // whichever decision is made - a single preview-then-decide transaction, not a multi-try lock
     // that survives repeated attempts.
    private boolean resolvePendingPreview(Player player, boolean accept) {
        if (pendingPreview.isEmpty()) {
            return false;
        }
        AffixData candidate = pendingPreview.get();
        pendingPreview = Optional.empty();

        ItemStack targetStack = getTargetStack();
        ItemStack recipeStack = getRecipeStack();
        ItemStack paymentStack = container.getItem(SLOT_PAYMENT);
        ItemStack lockStack = getLockStack();
        if (targetStack.isEmpty() || recipeStack.isEmpty() || paymentStack.isEmpty() || lockStack.isEmpty()) {
            return false;
        }

        if (accept) {
            targetStack.set(TumultuDataComponents.AFFIX_DATA.get(), candidate);
            AffixEffectApplier.apply(targetStack, candidate, player.registryAccess(), player.getRandom());
        }

        recipeStack.shrink(1);
        paymentStack.shrink(1);
        lockStack.shrink(1);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (index < SLOT_COUNT) {
            if (!this.moveItemStackTo(stackInSlot, SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            for (int benchSlot = 0; benchSlot < SLOT_COUNT && !moved; benchSlot++) {
                if (this.slots.get(benchSlot).mayPlace(stackInSlot)) {
                    moved = this.moveItemStackTo(stackInSlot, benchSlot, benchSlot + 1, false);
                }
            }
            if (!moved) {
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
