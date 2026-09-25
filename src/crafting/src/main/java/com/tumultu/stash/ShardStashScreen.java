package com.tumultu.stash;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The 5x3 grid and the target slot are plain vanilla {@code Slot}s, so deposit/withdraw needs no
 * custom rendering here. What's different from a normal container: the grid always shows exactly
 * one currency at a time, "the front of" its total - see {@link ShardStashMenu}'s doc comment for
 * how the visible cells and the uncapped hidden counter combine. Clicking a row in the scrollable
 * list below sends that currency's index as a menu-button click, which the server uses to sweep
 * the outgoing currency away and unpack the new one into the grid, then pushes a fresh sync. The
 * Apply button sends a separate sentinel id meaning "apply whatever's currently selected" - both
 * use the same mechanism the crafting bench does.
 */
public class ShardStashScreen extends AbstractContainerScreen<ShardStashMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("tumultu", "textures/gui/shard_stash.png");
    private static final int LIST_Y = 82;
    private static final int LIST_HEIGHT_ROWS = 5;
    private static final int ROW_SPACING = 20;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_WIDTH = 156;

    private final List<Button> rowButtons = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedIndex = -1;
    private Button applyButton;

    public ShardStashScreen(ShardStashMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 304);
    }

    @Override
    protected void init() {
        super.init();
        // Reads the server-synced selection (see ShardStashMenu#getSelectedCurrency) rather than
        // assuming 0, so the highlight is correct immediately even after reopening a stash that
        // resumed a different currency than the default.
        selectedIndex = menu.getSelectedCurrency();
        applyButton = Button.builder(Component.translatable("tumultu.stash.apply"), b ->
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, ShardStashMenu.APPLY_BUTTON)
        ).bounds(leftPos + 10, topPos + LIST_Y + LIST_HEIGHT_ROWS * ROW_SPACING + 4, ROW_WIDTH, ROW_HEIGHT).build();
        this.addRenderableWidget(applyButton);
        rebuildRows();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        rebuildRows();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverList(mouseX, mouseY) && ShardStashMenu.CURRENCY_COUNT > LIST_HEIGHT_ROWS) {
            int maxOffset = ShardStashMenu.CURRENCY_COUNT - LIST_HEIGHT_ROWS;
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(scrollY)));
            rebuildRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isOverList(double mouseX, double mouseY) {
        return mouseX >= leftPos && mouseX <= leftPos + imageWidth
                && mouseY >= topPos + LIST_Y && mouseY <= topPos + LIST_Y + LIST_HEIGHT_ROWS * ROW_SPACING;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, titleLabelX, titleLabelY, 4210752);
    }

    private void rebuildRows() {
        for (Button button : rowButtons) {
            this.removeWidget(button);
        }
        rowButtons.clear();

        int visibleCount = Math.min(LIST_HEIGHT_ROWS, ShardStashMenu.CURRENCY_COUNT);
        for (int row = 0; row < visibleCount; row++) {
            int index = scrollOffset + row;
            int total = menu.getCurrencyTotal(index);
            Component itemName = Component.translatable(ShardStashMenu.CURRENCIES.get(index).get().getDescriptionId());
            String prefix = index == selectedIndex ? "> " : "";
            Component label = Component.literal(prefix + itemName.getString() + " (" + total + ")");

            int y = LIST_Y + row * ROW_SPACING;
            Button button = Button.builder(label, b -> {
                selectedIndex = index;
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, index);
                rebuildRows();
            }).bounds(leftPos + 10, topPos + y, ROW_WIDTH, ROW_HEIGHT).build();
            rowButtons.add(button);
            this.addRenderableWidget(button);
        }

        // Nothing selected, or the selected currency's total is 0
        int selected = menu.getSelectedCurrency();
        applyButton.active = selected >= 0 && menu.getCurrencyTotal(selected) > 0;
    }
}
