package com.tumultu.combat;

import com.tumultu.TumultuMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

public class TumultuAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TumultuMod.MOD_ID);

    public static final Supplier<AttachmentType<DamageOverTimeData>> BLEED = ATTACHMENTS.register(
            "bleed", () -> AttachmentType.builder(() -> DamageOverTimeData.INACTIVE).build());

    public static final Supplier<AttachmentType<List<DamageOverTimeData>>> POISON = ATTACHMENTS.register(
            "poison", () -> AttachmentType.builder((Supplier<List<DamageOverTimeData>>) List::of).build());

    public static final Supplier<AttachmentType<BurnData>> BURN = ATTACHMENTS.register(
            "burn", () -> AttachmentType.builder(() -> BurnData.INACTIVE).build());

    public static final Supplier<AttachmentType<ShockedData>> SHOCKED = ATTACHMENTS.register(
            "shocked", () -> AttachmentType.builder(() -> ShockedData.INACTIVE).build());

    /** Builds up from fire damage, decays continuously - see {@link ExposureData}. */
    public static final Supplier<AttachmentType<ExposureData>> FIRE_EXPOSURE = ATTACHMENTS.register(
            "fire_exposure", () -> AttachmentType.builder(() -> ExposureData.INACTIVE).build());

    public static final Supplier<AttachmentType<ExposureData>> LIGHTNING_EXPOSURE = ATTACHMENTS.register(
            "lightning_exposure", () -> AttachmentType.builder(() -> ExposureData.INACTIVE).build());
}
