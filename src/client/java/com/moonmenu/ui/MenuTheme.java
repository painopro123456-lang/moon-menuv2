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

    public static int accentWithAlpha(int alpha) {
        return ((alpha & 0xFF) << 24) | (accent & 0x00FFFFFF);
    }
}
