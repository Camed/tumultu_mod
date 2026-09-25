package com.tumultu.affix;

import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.tooltip.TooltipAppender;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class AffixTooltipAppender implements TooltipAppender {

    @Override
    public void append(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                        @Nullable Player player, TooltipFlag tooltipFlag, Consumer<Component> builder) {
        AffixData data = stack.get(TumultuDataComponents.AFFIX_DATA.get());
        if (data == null || data.rarity() == ItemRarity.NORMAL) {
            return;
        }

        Level level = context.level();
        if (level == null) {
            return;
        }

        builder.accept(Component.translatable("tumultu.rarity." + data.rarity().getSerializedName())
                .withStyle(data.rarity().color()));

        Registry<AffixDefinition> registry = level.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);

        // first show prefix, then suffix
        appendAffixLines(builder, registry, data, AffixType.PREFIX);
        appendAffixLines(builder, registry, data, AffixType.SUFFIX);
    }

    private static void appendAffixLines(Consumer<Component> builder, Registry<AffixDefinition> registry, AffixData data, AffixType type) {
        for (RolledAffix rolled : data.affixes()) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def == null || def.type() != type) {
                continue;
            }

            boolean isImbued = data.imbuedAffixId().isPresent() && data.imbuedAffixId().get().equals(rolled.affixId());
            boolean isCrafted = data.craftedAffixId().isPresent() && data.craftedAffixId().get().equals(rolled.affixId());
            ChatFormatting color = isImbued ? ChatFormatting.LIGHT_PURPLE : isCrafted ? ChatFormatting.AQUA : ChatFormatting.BLUE;

            Component line = Component.literal(def.displayName() + " ")
                    .append(def.effect().describe(rolled.rolledValue()))
                    .withStyle(color);

            if (rolled.tierIndex() < def.tiers().size()) {
                AffixTier tier = def.tiers().get(rolled.tierIndex());
                boolean isPercentage = def.effect().isPercentage();
                String range = " (" + format(tier.minValue(), isPercentage) + " - " + format(tier.maxValue(), isPercentage) + " | T" + (def.tiers().toArray().length - rolled.tierIndex()) + ")";
                line = line.copy().append(Component.literal(range).withStyle(ChatFormatting.DARK_GRAY));
            }

            if (isImbued) {
                line = line.copy().append(Component.translatable("tumultu.imbued_tag").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            } else if (isCrafted) {
                line = line.copy().append(Component.translatable("tumultu.crafted_tag").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
            }

            builder.accept(line);
        }
    }

    private static String format(double value, boolean isPercentage) {
        double display = isPercentage ? value * 100.0 : value;
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(display);
    }
}
