package com.tumultu.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.List;
import java.util.function.Supplier;

// dev/debug shenanigans
public class DebugStatus {

    public static String describeBleed(LivingEntity entity) {
        AttachmentType<DamageOverTimeData> attachment = TumultuAttachments.BLEED.get();
        if (!entity.hasData(attachment)) {
            return "none";
        }
        DamageOverTimeData data = entity.getData(attachment);
        if (!data.isActive()) {
            return "none";
        }
        return String.format("%.2f dmg/s, %d ticks left", data.damagePerSecond(), data.ticksRemaining());
    }

    public static String describePoison(Level level, LivingEntity entity) {
        AttachmentType<List<DamageOverTimeData>> attachment = TumultuAttachments.POISON.get();
        if (!entity.hasData(attachment)) {
            return "none";
        }
        List<DamageOverTimeData> stacks = entity.getData(attachment);
        if (stacks.isEmpty()) {
            return "none";
        }

        double totalDps = stacks.stream().mapToDouble(DamageOverTimeData::damagePerSecond).sum();
        StringBuilder sb = new StringBuilder(String.format("%d stack(s), %.2f dmg/s total [", stacks.size(), totalDps));
        for (int i = 0; i < stacks.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            DamageOverTimeData stack = stacks.get(i);
            Entity source = level.getEntity(stack.sourceId());
            String sourceName = source != null ? source.getName().getString() : "unknown";
            sb.append(String.format("%.2f dmg/s, %d ticks left (from %s)", stack.damagePerSecond(), stack.ticksRemaining(), sourceName));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String describeBurn(LivingEntity entity) {
        AttachmentType<BurnData> attachment = TumultuAttachments.BURN.get();
        if (!entity.hasData(attachment)) {
            return "none";
        }
        BurnData data = entity.getData(attachment);
        if (!data.isActive()) {
            return "none";
        }
        float exposure = describeExposureValue(entity, TumultuAttachments.FIRE_EXPOSURE);
        float dps = (float) ((CombatEventHandler.BURN_BASE_DAMAGE_PER_SECOND
                + exposure * CombatEventHandler.BURN_DAMAGE_PER_EXPOSURE_POINT) * (1 + data.dotMultiplier()));
        return String.format("%.2f dmg/s (fire exposure %.0f%%), %d ticks left", dps, exposure, data.ticksRemaining());
    }

    public static String describeFireExposure(LivingEntity entity) {
        return String.format("%.0f%%", describeExposureValue(entity, TumultuAttachments.FIRE_EXPOSURE));
    }

    public static String describeLightningExposure(LivingEntity entity) {
        return String.format("%.0f%%", describeExposureValue(entity, TumultuAttachments.LIGHTNING_EXPOSURE));
    }

    private static float describeExposureValue(LivingEntity entity, Supplier<AttachmentType<ExposureData>> attachmentSupplier) {
        AttachmentType<ExposureData> attachment = attachmentSupplier.get();
        return entity.hasData(attachment) ? entity.getData(attachment).value() : 0f;
    }

    public static String describeFreeze(LivingEntity entity) {
        if (!entity.isFreezing()) {
            return "none";
        }
        return String.format("%.0f%% frozen", entity.getPercentFrozen() * 100);
    }

    public static String describeShocked(LivingEntity entity) {
        AttachmentType<ShockedData> attachment = TumultuAttachments.SHOCKED.get();
        if (!entity.hasData(attachment)) {
            return "none";
        }
        ShockedData data = entity.getData(attachment);
        if (!data.isActive()) {
            return "none";
        }
        return data.ticksRemaining() + " ticks left";
    }
}
