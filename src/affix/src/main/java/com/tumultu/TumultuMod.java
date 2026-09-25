package com.tumultu;

/**
 * Canonical mod id, defined here rather than on the root {@code Tumultu} class - the root module
 * depends on every domain subproject (including this one), so nothing in a domain subproject can
 * depend back on root without creating a circular Gradle dependency. {@code affix} is the one
 * subproject every other domain already depends on, so it's the natural home for a constant every
 * domain needs. {@code Tumultu.MOD_ID} re-exposes this same value for root's own files.
 */
public final class TumultuMod {
    public static final String MOD_ID = "tumultu";

    private TumultuMod() {}
}
