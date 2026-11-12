package com.moonmenu.ui;

public final class MenuTheme {
    private static int accent = 0xFF9AAFCB;

    private MenuTheme() {}

    public static int accent() {
        return accent;
    }

    public static void setAccent(int color) {
        accent = 0xFF000000 | (color & 0x00FFFFFF);
    }

    public static float hueFraction() {
        float r = ((accent >> 16) & 255) / 255.0f;
        float g = ((accent >> 8) & 255) / 255.0f;
        float b = (accent & 255) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        if (delta == 0) return 0.0f;
        float hue;
        if (max == r) hue = ((g - b) / delta) % 6.0f;
        else if (max == g) hue = (b - r) / delta + 2.0f;
        else hue = (r - g) / delta + 4.0f;
        return (hue / 6.0f + 1.0f) % 1.0f;
    }

    public static int accentWithAlpha(int alpha) {
        return ((alpha & 0xFF) << 24) | (accent & 0x00FFFFFF);
    }

    /** Blend the selected accent into a base UI surface while preserving its alpha. */
    public static int tint(int base, float amount) {
        float a = Math.max(0.0f, Math.min(1.0f, amount));
        int r = blend((base >> 16) & 255, (accent >> 16) & 255, a);
        int g = blend((base >> 8) & 255, (accent >> 8) & 255, a);
        int b = blend(base & 255, accent & 255, a);
        return (base & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private static int blend(int from, int to, float amount) {
        return Math.round(from + (to - from) * amount);
    }
}
