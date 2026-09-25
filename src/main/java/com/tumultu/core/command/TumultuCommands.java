package com.tumultu.core.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.AffixEffectApplier;
import com.tumultu.affix.AffixTier;
import com.tumultu.affix.ItemRarity;
import com.tumultu.affix.RolledAffix;
import com.tumultu.affix.effect.ElementKind;
import com.tumultu.combat.CombatDevLog;
import com.tumultu.combat.CombatEventHandler;
import com.tumultu.combat.CombatTickHandler;
import com.tumultu.combat.DebugStatus;
import com.tumultu.combat.PlayerCombatStats;
import com.tumultu.registry.CurrencyItems;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
public class TumultuCommands {

    private static final SimpleCommandExceptionType ERROR_EMPTY_HAND =
            new SimpleCommandExceptionType(Component.literal("You must be holding an item in your main hand."));
    private static final SimpleCommandExceptionType ERROR_NO_ELIGIBLE_TIER =
            new SimpleCommandExceptionType(Component.literal("This affix has no tier eligible for the item in your main hand."));
    private static final SimpleCommandExceptionType ERROR_TIER_NOT_ELIGIBLE =
            new SimpleCommandExceptionType(Component.literal("That tier isn't eligible for the item in your main hand (wrong material/slot, or out of range)."));
    private static final SimpleCommandExceptionType ERROR_NOT_LIVING =
            new SimpleCommandExceptionType(Component.literal("That target isn't a living entity."));

    private static final List<Supplier<Item>> SHARDS = List.of(
            CurrencyItems.SHAPING_SHARD,
            CurrencyItems.GROWTH_SHARD,
            CurrencyItems.HEIGHTENING_SHARD,
            CurrencyItems.INFUSION_SHARD,
            CurrencyItems.CLEANSING_SHARD,
            CurrencyItems.TWISTING_SHARD,
            CurrencyItems.PEAKING_SHARD,
            CurrencyItems.SEVERING_SHARD,
            CurrencyItems.WEAVING_SHARD,
            CurrencyItems.ENDFUSING_SHARD
    );

    private static final int SHARD_COUNT = 32;

    public static void register(RegisterCommandsEvent event) {
        CommandBuildContext buildContext = event.getBuildContext();

        event.getDispatcher().register(
                Commands.literal("tumultuitems")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("kit").executes(TumultuCommands::giveKit))
                        .then(Commands.literal("affix")
                                .then(Commands.argument("affix", ResourceArgument.resource(buildContext, TumultuRegistries.AFFIX_KEY))
                                        .executes(ctx -> giveAffix(ctx, -1))
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(0))
                                                .executes(ctx -> giveAffix(ctx, IntegerArgumentType.getInteger(ctx, "tier"))))))
                        .then(Commands.literal("affixclear").executes(TumultuCommands::clearAffixes))
                        .then(Commands.literal("status")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(TumultuCommands::showStatus)))
                        .then(Commands.literal("debug")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(TumultuCommands::enableDebugOverlay))
                                .then(Commands.literal("off").executes(TumultuCommands::disableDebugOverlay)))
                        .then(Commands.literal("inflict")
                                .then(inflictBranch(true))
                                .then(inflictBranch(false))
                                .then(elementalDamageBranch(ElementKind.FIRE))
                                .then(elementalDamageBranch(ElementKind.COLD))
                                .then(elementalDamageBranch(ElementKind.LIGHTNING))
                                .then(Commands.literal("burn")
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(ctx -> inflictBurn(ctx, CombatEventHandler.DOT_DURATION_TICKS))
                                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> inflictBurn(ctx, IntegerArgumentType.getInteger(ctx, "duration"))))))
                                .then(Commands.literal("shock")
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(ctx -> inflictShock(ctx, CombatEventHandler.DOT_DURATION_TICKS))
                                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> inflictShock(ctx, IntegerArgumentType.getInteger(ctx, "duration"))))))
                                .then(Commands.literal("freeze")
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(TumultuCommands::inflictFreeze)))))
                        .then(Commands.literal("stats")
                                .executes(TumultuCommands::showSelfStats)
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(TumultuCommands::showTargetStats)))
                        .then(Commands.literal("devlog").executes(TumultuCommands::toggleDevLog))
        );
    }

    private static int toggleDevLog(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        boolean enabled = CombatDevLog.toggle(player);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Combat devlog " + (enabled ? "enabled" : "disabled") + (enabled ? " - every hit you land will be printed to chat." : ".")), true);
        return 1;
    }

    private static int showSelfStats(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return printStats(ctx, ctx.getSource().getPlayerOrException());
    }

    private static int showTargetStats(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(ctx, "target");
        if (!(target instanceof LivingEntity living)) {
            throw ERROR_NOT_LIVING.create();
        }
        return printStats(ctx, living);
    }

    private static int printStats(CommandContext<CommandSourceStack> ctx, LivingEntity entity) {
        Registry<AffixDefinition> registry = entity.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);

        double maxHealth = entity.getAttributeValue(Attributes.MAX_HEALTH);
        double armor = PlayerCombatStats.trueArmor(entity, registry);
        double toughness = entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        double moveSpeed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double knockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        // Not getAttributeValue: LUCK isn't registered on most non-player entities at all (unlike
        // the attributes above), and this command can target any LivingEntity - getAttributeValue
        // throws "Can't find attribute" instead of returning a default in that case.
        AttributeInstance luckAttribute = entity.getAttribute(Attributes.LUCK);
        double luck = luckAttribute != null ? luckAttribute.getValue() : 0.0;

        double fireRes = PlayerCombatStats.elementalResistance(entity, ElementKind.FIRE, registry);
        double fireResRaw = fireRes + PlayerCombatStats.elementalResistanceOvercap(entity, ElementKind.FIRE, registry);
        double coldRes = PlayerCombatStats.elementalResistance(entity, ElementKind.COLD, registry);
        double coldResRaw = coldRes + PlayerCombatStats.elementalResistanceOvercap(entity, ElementKind.COLD, registry);
        double lightningRes = PlayerCombatStats.elementalResistance(entity, ElementKind.LIGHTNING, registry);
        double lightningResRaw = lightningRes + PlayerCombatStats.elementalResistanceOvercap(entity, ElementKind.LIGHTNING, registry);
        double penetration = PlayerCombatStats.elementalPenetration(entity, registry);
        double armorReduction = 1 - PlayerCombatStats.armorDamageMultiplier(entity, registry);
        double physReduction = PlayerCombatStats.physicalDamageReduction(entity, registry);
        double physReductionRaw = PlayerCombatStats.rawPhysicalDamageReduction(entity, registry);
        double finalPhysicalReduction = 1 - PlayerCombatStats.finalPhysicalDamageMultiplier(entity, registry);
        double finalPhysicalReductionRaw = PlayerCombatStats.rawFinalPhysicalDamageReduction(entity, registry);
        double thorns = PlayerCombatStats.thornsMultiplier(entity, registry);
        double critChance = PlayerCombatStats.critChance(entity, registry);
        double critDamage = PlayerCombatStats.critDamagePercent(entity, registry);
        double critDamageReduction = PlayerCombatStats.critDamageReduction(entity, registry);
        double regen = PlayerCombatStats.lifeRegenerationPerSecond(entity, registry);
        boolean poisonImmune = PlayerCombatStats.isImmuneToPoison(entity, registry);

        Component name = entity.getName();
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("=== " + name.getString() + "'s Stats ==="), false);
        source.sendSuccess(() -> Component.literal(String.format("Health: %.1f / %.1f", entity.getHealth(), maxHealth)), false);
        source.sendSuccess(() -> Component.literal(String.format("Armor: %.1f | Toughness: %.1f | Armor Reduction: %.0f%% | Knockback Resist: %.0f%%", armor, toughness, armorReduction * 100, knockbackRes * 100)), false);
        source.sendSuccess(() -> Component.literal(String.format("Movement Speed: %.3f | Luck: %.1f", moveSpeed, luck)), false);
        source.sendSuccess(() -> Component.literal("Resistance - Fire: " + percent(fireRes, fireResRaw)
                + " Cold: " + percent(coldRes, coldResRaw) + " Lightning: " + percent(lightningRes, lightningResRaw)), false);
        source.sendSuccess(() -> Component.literal(String.format("Elemental Penetration: %.0f%%", penetration * 100)), false);
        source.sendSuccess(() -> Component.literal("Additional Physical Reduction: " + percent(physReduction, physReductionRaw)), false);
        source.sendSuccess(() -> Component.literal("Final Physical Reduction: " + percent(finalPhysicalReduction, finalPhysicalReductionRaw)), false);
        source.sendSuccess(() -> Component.literal(String.format("Thorns Multiplier: +%.0f%%", thorns * 100)), false);
        source.sendSuccess(() -> Component.literal(String.format("Crit Chance: %.0f%% | Crit Damage: +%.0f%%", critChance * 100, critDamage * 100)), false);
        source.sendSuccess(() -> Component.literal(String.format("Crit Damage Reduction: -%.0f%%", critDamageReduction * 100)), false);
        source.sendSuccess(() -> Component.literal(String.format("Life Regeneration: %.1f/s", regen)), false);
        source.sendSuccess(() -> Component.literal("Poison Immune: " + (poisonImmune ? "Yes" : "No")), false);
        return 1;
    }

    private static String percent(double capped, double raw) {
        if (raw > capped + 1e-9) {
            return String.format("%.0f%%(%.0f%%)", capped * 100, raw * 100);
        }
        return String.format("%.0f%%", capped * 100);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> inflictBranch(boolean bleed) {
        return Commands.literal(bleed ? "bleed" : "poison")
                .then(Commands.argument("target", EntityArgument.entity())
                        .executes(ctx -> inflict(ctx, bleed, CombatEventHandler.DOT_BASE_DAMAGE_PER_SECOND, CombatEventHandler.DOT_DURATION_TICKS))
                        .then(Commands.argument("dps", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> inflict(ctx, bleed, DoubleArgumentType.getDouble(ctx, "dps"), CombatEventHandler.DOT_DURATION_TICKS))
                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                        .executes(ctx -> inflict(ctx, bleed, DoubleArgumentType.getDouble(ctx, "dps"), IntegerArgumentType.getInteger(ctx, "duration"))))));
    }

    private static int inflict(CommandContext<CommandSourceStack> ctx, boolean bleed, double dps, int durationTicks) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Entity targetEntity = EntityArgument.getEntity(ctx, "target");
        if (!(targetEntity instanceof LivingEntity target)) {
            throw ERROR_NOT_LIVING.create();
        }

        if (bleed) {
            CombatEventHandler.inflictBleed(target, player.getUUID(), (float) dps, durationTicks);
        } else {
            CombatEventHandler.inflictPoison(target, player.getUUID(), (float) dps, durationTicks);
        }

        String label = bleed ? "Bleed" : "Poison";
        Component name = target.getName();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Applied " + label + " (" + dps + " dmg/s, " + durationTicks + " ticks) to " + name.getString() + "."), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> elementalDamageBranch(ElementKind element) {
        return Commands.literal(element.getSerializedName())
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> inflictElementalDamage(ctx, element))));
    }

    private static int inflictElementalDamage(CommandContext<CommandSourceStack> ctx, ElementKind element) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Entity targetEntity = EntityArgument.getEntity(ctx, "target");
        if (!(targetEntity instanceof LivingEntity target)) {
            throw ERROR_NOT_LIVING.create();
        }

        double amount = DoubleArgumentType.getDouble(ctx, "amount");
        CombatEventHandler.inflictElementalDamage(target, player.getUUID(), element, (float) amount);

        String label = element.displayName();
        Component name = target.getName();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Dealt " + amount + " " + label + " damage to " + name.getString()
                        + " (queued for next tick - resistance/exposure apply as usual)."), true);
        return 1;
    }

    private static int inflictBurn(CommandContext<CommandSourceStack> ctx, int durationTicks) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Entity targetEntity = EntityArgument.getEntity(ctx, "target");
        if (!(targetEntity instanceof LivingEntity target)) {
            throw ERROR_NOT_LIVING.create();
        }

        CombatEventHandler.inflictBurn(target, player.getUUID(), 0, durationTicks);
        Component name = target.getName();
        ctx.getSource().sendSuccess(() -> Component.literal("Applied Burn (" + durationTicks + " ticks) to " + name.getString() + "."), true);
        return 1;
    }

    private static int inflictShock(CommandContext<CommandSourceStack> ctx, int durationTicks) throws CommandSyntaxException {
        Entity targetEntity = EntityArgument.getEntity(ctx, "target");
        if (!(targetEntity instanceof LivingEntity target)) {
            throw ERROR_NOT_LIVING.create();
        }

        CombatEventHandler.inflictShock(target, durationTicks);
        Component name = target.getName();
        ctx.getSource().sendSuccess(() -> Component.literal("Applied Shocked (" + durationTicks + " ticks) to " + name.getString() + "."), true);
        return 1;
    }

    private static int inflictFreeze(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity targetEntity = EntityArgument.getEntity(ctx, "target");
        if (!(targetEntity instanceof LivingEntity target)) {
            throw ERROR_NOT_LIVING.create();
        }

        int amount = Math.min(target.getTicksRequiredToFreeze(), IntegerArgumentType.getInteger(ctx, "amount"));
        target.setTicksFrozen(amount);
        Component name = target.getName();
        ctx.getSource().sendSuccess(() -> Component.literal("Set " + name.getString() + "'s ticksFrozen to " + amount + "."), true);
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity targetEntity = EntityArgument.getEntity(ctx, "target");
        if (!(targetEntity instanceof LivingEntity target)) {
            throw ERROR_NOT_LIVING.create();
        }

        Level level = ctx.getSource().getLevel();
        Component name = target.getName();
        ctx.getSource().sendSuccess(() -> Component.literal(name.getString() + " - Bleed: " + DebugStatus.describeBleed(target)), false);
        ctx.getSource().sendSuccess(() -> Component.literal(name.getString() + " - Poison: " + DebugStatus.describePoison(level, target)), false);
        ctx.getSource().sendSuccess(() -> Component.literal(name.getString() + " - Burn: " + DebugStatus.describeBurn(target)
                + " | Fire Exposure: " + DebugStatus.describeFireExposure(target)), false);
        ctx.getSource().sendSuccess(() -> Component.literal(name.getString() + " - Freeze: " + DebugStatus.describeFreeze(target)), false);
        ctx.getSource().sendSuccess(() -> Component.literal(name.getString() + " - Shocked: " + DebugStatus.describeShocked(target)
                + " | Lightning Exposure: " + DebugStatus.describeLightningExposure(target)), false);
        return 1;
    }

    private static int enableDebugOverlay(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Entity target = EntityArgument.getEntity(ctx, "target");
        if (!(target instanceof LivingEntity)) {
            throw ERROR_NOT_LIVING.create();
        }

        CombatTickHandler.DEBUG_TARGETS.put(player.getUUID(), target.getUUID());
        Component name = target.getName();
        ctx.getSource().sendSuccess(() -> Component.literal("Debug overlay enabled - tracking " + name.getString() + " in your action bar."), true);
        return 1;
    }

    private static int disableDebugOverlay(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        CombatTickHandler.DEBUG_TARGETS.remove(player.getUUID());
        ctx.getSource().sendSuccess(() -> Component.literal("Debug overlay disabled."), true);
        return 1;
    }

    private static int giveAffix(CommandContext<CommandSourceStack> ctx, int requestedTier) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_EMPTY_HAND.create();
        }

        Holder.Reference<AffixDefinition> affixHolder = ResourceArgument.getResource(ctx, "affix", TumultuRegistries.AFFIX_KEY);
        AffixDefinition def = affixHolder.value();
        Identifier affixId = affixHolder.key().identifier();

        int tierIndex = requestedTier;
        AffixTier tier;
        if (requestedTier >= 0) {
            if (requestedTier >= def.tiers().size() || !def.tiers().get(requestedTier).isApplicableTo(stack)) {
                throw ERROR_TIER_NOT_ELIGIBLE.create();
            }
            tier = def.tiers().get(requestedTier);
        } else {
            tier = null;
            for (int t = def.tiers().size() - 1; t >= 0; t--) {
                if (def.tiers().get(t).isApplicableTo(stack)) {
                    tierIndex = t;
                    tier = def.tiers().get(t);
                    break;
                }
            }
            if (tier == null) {
                throw ERROR_NO_ELIGIBLE_TIER.create();
            }
        }

        RegistryAccess registryAccess = ctx.getSource().registryAccess();
        RandomSource random = player.getRandom();
        double value = tier.roll(random);

        AffixData existing = stack.get(TumultuDataComponents.AFFIX_DATA.get());
        if (existing == null) {
            existing = AffixData.EMPTY;
        }

        // Replace rather than add if this exact affix is already present, so re-running the
        // command to change tier/value doesn't leave the old roll stacked alongside the new one.
        List<RolledAffix> affixes = new ArrayList<>();
        for (RolledAffix rolled : existing.affixes()) {
            if (!rolled.affixId().equals(affixId)) {
                affixes.add(rolled);
            }
        }
        affixes.add(new RolledAffix(affixId, tierIndex, value));

        ItemRarity rarity = existing.rarity() == ItemRarity.NORMAL ? ItemRarity.RARE : existing.rarity();
        AffixData newData = new AffixData(rarity, List.copyOf(affixes), existing.endfused());

        stack.set(TumultuDataComponents.AFFIX_DATA.get(), newData);
        AffixEffectApplier.apply(stack, newData, registryAccess, random);

        int finalTierIndex = tierIndex;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Applied " + affixId + " (tier " + finalTierIndex + ", value " + value + ") to held item."), true);
        return 1;
    }

    private static int clearAffixes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_EMPTY_HAND.create();
        }

        RegistryAccess registryAccess = ctx.getSource().registryAccess();
        stack.set(TumultuDataComponents.AFFIX_DATA.get(), AffixData.EMPTY);
        AffixEffectApplier.apply(stack, AffixData.EMPTY, registryAccess, player.getRandom());

        ctx.getSource().sendSuccess(() -> Component.literal("Cleared affixes from held item."), true);
        return 1;
    }

    private static int giveKit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Inventory inventory = player.getInventory();

        for (Supplier<Item> shard : SHARDS) {
            inventory.add(new ItemStack(shard.get(), SHARD_COUNT));
        }

        inventory.add(new ItemStack(Items.DIAMOND_SWORD));
        inventory.add(new ItemStack(Items.DIAMOND_PICKAXE));
        inventory.add(new ItemStack(Items.DIAMOND_HELMET));
        inventory.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
        inventory.add(new ItemStack(Items.DIAMOND_LEGGINGS));
        inventory.add(new ItemStack(Items.DIAMOND_BOOTS));

        ctx.getSource().sendSuccess(() -> Component.literal("Gave testing kit: all shards, diamond sword/pickaxe/armor."), true);
        return 1;
    }
}
