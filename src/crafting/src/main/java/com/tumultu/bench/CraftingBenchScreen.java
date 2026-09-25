package com.tumultu.bench;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.AffixTier;
import com.tumultu.affix.BenchCrafting;
import com.tumultu.affix.RecipeCategory;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuRecipeItems;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingBenchScreen extends AbstractContainerScreen<CraftingBenchMenu> {
    private static final int BUTTON_WIDTH = 156;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_SPACING = 20;
    private static final int VISIBLE_ROWS = 3;
    private static final int BUTTON_LIST_Y = 55;
    private static final int BUTTON_LIST_HEIGHT = VISIBLE_ROWS * BUTTON_SPACING;

    private final List<Button> affixButtons = new ArrayList<>();
    private int scrollOffset = 0;
    private int affixCount = 0;
    private Component infoMessage = null;
    private record PendingPreview(Identifier affixId, int tier, double value) {}
    private PendingPreview pendingPreview = null;
    private static CraftingBenchScreen currentlyOpen = null;

    public static void handlePreviewResult(Identifier affixId, int tier, double value) {
        if (currentlyOpen != null) {
            currentlyOpen.pendingPreview = new PendingPreview(affixId, tier, value);
            currentlyOpen.rebuildButtons();
        }
    }

    public CraftingBenchScreen(CraftingBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 240);
    }

    @Override
    protected void init() {
        super.init();
        currentlyOpen = this;
        rebuildButtons();
    }
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, titleLabelX, titleLabelY, 4210752);
    }

    @Override
    public void removed() {
        if (currentlyOpen == this) {
            currentlyOpen = null;
        }
        super.removed();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        rebuildButtons();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverButtonList(mouseX, mouseY) && affixCount > VISIBLE_ROWS) {
            int maxOffset = affixCount - VISIBLE_ROWS;
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(scrollY)));
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isOverButtonList(double mouseX, double mouseY) {
        return mouseX >= leftPos && mouseX <= leftPos + imageWidth
                && mouseY >= topPos + BUTTON_LIST_Y && mouseY <= topPos + BUTTON_LIST_Y + BUTTON_LIST_HEIGHT;
    }
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("tumultu", "textures/gui/crafting_bench.png");

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        if (infoMessage != null) {
            graphics.textWithWordWrap(this.font, infoMessage, leftPos + 10, topPos + BUTTON_LIST_Y - 6, imageWidth - 20, 0xFFAAAAAA);
        }
    }

    private void rebuildButtons() {
        for (Button button : affixButtons) {
            this.removeWidget(button);
        }
        affixButtons.clear();
        infoMessage = null;
        affixCount = 0;

        if (pendingPreview != null) {
            if (menu.getLockStack().isEmpty()) {
                // The lock item was removed/consumed elsewhere - drop the stale preview.
                pendingPreview = null;
            } else {
                renderPendingPreview();
                return;
            }
        }

        ItemStack targetStack = menu.getTargetStack();
        ItemStack recipeStack = menu.getRecipeStack();
        if (targetStack.isEmpty() || recipeStack.isEmpty()) {
            return;
        }

        AffixData existing = targetStack.get(TumultuDataComponents.AFFIX_DATA.get());
        if (existing == null) {
            existing = AffixData.EMPTY;
        }

        if (CraftingBenchMenu.isCleansingShard(recipeStack)) {
            if (existing.craftedAffixId().isPresent()) {
                addAffixButton(Component.translatable("tumultu.bench.remove_crafted"), 0, 0);
            } else {
                infoMessage = Component.translatable("tumultu.bench.nothing_to_remove");
            }
            return;
        }

        if (existing.craftedAffixId().isPresent()) {
            infoMessage = Component.translatable("tumultu.bench.already_crafted");
            return;
        }

        Optional<RecipeCategory> category = TumultuRecipeItems.categoryOf(recipeStack.getItem());
        if (category.isEmpty() || minecraft == null || minecraft.level == null) {
            return;
        }

        List<BenchCrafting.CraftableAffix> current =
                BenchCrafting.validCategoryAffixes(minecraft.level.registryAccess(), targetStack, existing, category.get());
        affixCount = current.size();
        scrollOffset = affixCount <= VISIBLE_ROWS ? 0 : Math.max(0, Math.min(scrollOffset, affixCount - VISIBLE_ROWS));

        int visibleCount = Math.min(VISIBLE_ROWS, affixCount);
        for (int row = 0; row < visibleCount; row++) {
            int index = scrollOffset + row;
            BenchCrafting.CraftableAffix affix = current.get(index);
            addAffixButton(Component.literal(affix.def().displayName()), index, row);
        }
    }

    private void addAffixButton(Component label, int buttonId, int row) {
        Button button = Button.builder(
                label,
                b -> Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, buttonId)
        ).bounds(leftPos + 10, topPos + BUTTON_LIST_Y + row * BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        affixButtons.add(button);
        this.addRenderableWidget(button);
    }

    private void renderPendingPreview() {
        PendingPreview preview = pendingPreview;
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        Registry<AffixDefinition> registry = minecraft.level.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        AffixDefinition def = registry.getValue(preview.affixId());

        Component previewLine;
        if (def != null) {
            previewLine = Component.literal(def.displayName() + " ").append(def.effect().describe(preview.value()));
            if (preview.tier() < def.tiers().size()) {
                AffixTier tierRange = def.tiers().get(preview.tier());
                boolean isPercentage = def.effect().isPercentage();
                String range = " (" + formatValue(tierRange.minValue(), isPercentage) + " - "
                        + formatValue(tierRange.maxValue(), isPercentage) + ")";
                previewLine = previewLine.copy().append(Component.literal(range).withStyle(ChatFormatting.DARK_GRAY));
            }
            // Internally tier 0 is the worst and the last index is the best,
            // but displayed tier numbers should read the other way round - Tier 1
            // is always the best, matching the mob/structure tier convention used elsewhere.
            int displayedTier = def.tiers().size() - preview.tier();
            previewLine = previewLine.copy().append(Component.literal(" [Tier " + displayedTier + "]")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            previewLine = Component.literal(preview.affixId().toString());
        }
        infoMessage = Component.translatable("tumultu.bench.preview_prefix").append(previewLine);

        // The preview text (name + value + tier range + tier number) can wrap to 2-3 lines, so
        // the buttons sit at fixed pixel offsets built in extra headroom, rather than the normal
        // row grid used elsewhere - lines up close enough with the candidate-list layout without
        // needing a taller panel just for this one screen.
        addDecisionButtonAtY(Component.translatable("tumultu.bench.accept_preview"),
                CraftingBenchMenu.ACCEPT_PREVIEW_BUTTON, BUTTON_LIST_Y + 20);
        addDecisionButtonAtY(Component.translatable("tumultu.bench.reject_preview"),
                CraftingBenchMenu.REJECT_PREVIEW_BUTTON, BUTTON_LIST_Y + 42);
    }

    private static String formatValue(double value, boolean isPercentage) {
        double display = isPercentage ? value * 100.0 : value;
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(display);
    }
    private void addDecisionButtonAtY(Component label, int buttonId, int yOffset) {
        Button button = Button.builder(label, b -> {
            pendingPreview = null;
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }).bounds(leftPos + 10, topPos + yOffset, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        affixButtons.add(button);
        this.addRenderableWidget(button);
    }
}
