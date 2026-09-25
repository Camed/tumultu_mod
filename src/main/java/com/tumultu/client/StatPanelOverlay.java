package com.tumultu.client;

import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.effect.ElementKind;
import com.tumultu.combat.PlayerCombatStats;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import top.theillusivec4.curios.api.client.ICuriosScreen;

import java.util.List;
public class StatPanelOverlay {

    private static final int PANEL_GAP = 8;
    private static final int PANEL_PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    private static final int BACKGROUND_COLOR = 0xC0101010;
    private static final int BORDER_COLOR = 0xFF3F3F3F;
    private static final int HEADER_COLOR = 0xFFFFAA00;
    private static final int TEXT_COLOR = 0xFFE0E0E0;

    private StatPanelOverlay() {
    }

    @SubscribeEvent
    public static void onScreenRenderForeground(ScreenEvent.Render.Foreground event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                || !(screen instanceof InventoryScreen || screen instanceof ICuriosScreen)) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int topPos = screen.getTopPos();
        int panelX = screen.getLeftPos() + screen.getImageWidth() + PANEL_GAP;

        List<String> lines = buildStatLines(player);
        Font font = Minecraft.getInstance().font;
        int panelWidth = font.width("Player Stats") + PANEL_PADDING * 2;
        for (String line : lines) {
            panelWidth = Math.max(panelWidth, font.width(line) + PANEL_PADDING * 2);
        }
        int panelHeight = PANEL_PADDING * 2 + LINE_HEIGHT * (lines.size() + 1);

        if (panelX + panelWidth > screen.width) {
            return;
        }

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        graphics.fill(panelX, topPos, panelX + panelWidth, topPos + panelHeight, BORDER_COLOR);
        graphics.fill(panelX + 1, topPos + 1, panelX + panelWidth - 1, topPos + panelHeight - 1, BACKGROUND_COLOR);

        int textX = panelX + PANEL_PADDING;
        int textY = topPos + PANEL_PADDING;
        graphics.text(font, "Player Stats", textX, textY, HEADER_COLOR);
        textY += LINE_HEIGHT;
        for (String line : lines) {
            graphics.text(font, line, textX, textY, TEXT_COLOR);
            textY += LINE_HEIGHT;
        }
    }

    private static List<String> buildStatLines(Player player) {
        Registry<AffixDefinition> registry = player.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);

        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        double armor = PlayerCombatStats.trueArmor(player, registry);
        double toughness = player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        double moveSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double knockbackRes = player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double luck = player.getAttributeValue(Attributes.LUCK);

        double fireRes = PlayerCombatStats.elementalResistance(player, ElementKind.FIRE, registry);
        double fireResRaw = fireRes + PlayerCombatStats.elementalResistanceOvercap(player, ElementKind.FIRE, registry);
        double coldRes = PlayerCombatStats.elementalResistance(player, ElementKind.COLD, registry);
        double coldResRaw = coldRes + PlayerCombatStats.elementalResistanceOvercap(player, ElementKind.COLD, registry);
        double lightningRes = PlayerCombatStats.elementalResistance(player, ElementKind.LIGHTNING, registry);
        double lightningResRaw = lightningRes + PlayerCombatStats.elementalResistanceOvercap(player, ElementKind.LIGHTNING, registry);
        double penetration = PlayerCombatStats.elementalPenetration(player, registry);
        double armorReduction = 1 - PlayerCombatStats.armorDamageMultiplier(player, registry);
        double physReduction = PlayerCombatStats.physicalDamageReduction(player, registry);
        double physReductionRaw = PlayerCombatStats.rawPhysicalDamageReduction(player, registry);
        double finalPhysicalReduction = 1 - PlayerCombatStats.finalPhysicalDamageMultiplier(player, registry);
        double finalPhysicalReductionRaw = PlayerCombatStats.rawFinalPhysicalDamageReduction(player, registry);
        double thorns = PlayerCombatStats.thornsMultiplier(player, registry);
        double critChance = PlayerCombatStats.critChance(player, registry);
        double critDamage = PlayerCombatStats.critDamagePercent(player, registry);
        double critDamageReduction = PlayerCombatStats.critDamageReduction(player, registry);
        double regen = PlayerCombatStats.lifeRegenerationPerSecond(player, registry);
        boolean poisonImmune = PlayerCombatStats.isImmuneToPoison(player, registry);

        return List.of(
                String.format("Health: %.1f / %.1f", player.getHealth(), maxHealth),
                String.format("Armor: %.1f  Toughness: %.1f", armor, toughness),
                String.format("Armor Reduction: %.0f%%", armorReduction * 100),
                String.format("Knockback Resist: %.0f%%", knockbackRes * 100),
                String.format("Movement Speed: %.3f", moveSpeed),
                String.format("Luck: %.1f", luck),
                percentWithOvercap("Fire Resist", fireRes, fireResRaw),
                percentWithOvercap("Cold Resist", coldRes, coldResRaw),
                percentWithOvercap("Lightning Resist", lightningRes, lightningResRaw),
                String.format("Elemental Penetration: %.0f%%", penetration * 100),
                percentWithOvercap("Additional Physical Reduction", physReduction, physReductionRaw),
                percentWithOvercap("Final Physical Reduction", finalPhysicalReduction, finalPhysicalReductionRaw),
                String.format("Thorns: +%.0f%%", thorns * 100),
                String.format("Crit Chance: %.0f%%", critChance * 100),
                String.format("Crit Damage: +%.0f%%", critDamage * 100),
                String.format("Crit Dmg Reduction: -%.0f%%", critDamageReduction * 100),
                String.format("Life Regen: %.1f/s", regen),
                "Poison Immune: " + (poisonImmune ? "Yes" : "No")
        );
    }

    private static String percentWithOvercap(String label, double capped, double raw) {
        if (raw > capped + 1e-9) {
            return String.format("%s: %.0f%% (%.0f%%)", label, capped * 100, raw * 100);
        }
        return String.format("%s: %.0f%%", label, capped * 100);
    }
}
