package com.tumultu.zones;

import com.tumultu.TumultuMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ZoneAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TumultuMod.MOD_ID);

    public static final Supplier<AttachmentType<Integer>> LAST_WORLD_TIER = ATTACHMENTS.register(
            "last_world_tier", () -> AttachmentType.builder(() -> WorldTier.MIN).build());
}
