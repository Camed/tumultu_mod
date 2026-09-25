package com.tumultu.affix.effect;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ElementKind implements StringRepresentable {
    FIRE("fire", "Fire"),
    COLD("cold", "Cold"),
    LIGHTNING("lightning", "Lightning");

    public static final Codec<ElementKind> CODEC = StringRepresentable.fromEnum(ElementKind::values);

    private final String id;
    private final String displayName;

    ElementKind(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
