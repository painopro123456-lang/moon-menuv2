package com.moonmenu.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

final class GlassSurface {
    private GlassSurface() {}

    static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius) {
        rounded(g, x + 3, y + 7, w, h, radius, 0x70000000);
        rounded(g, x + 1, y + 3, w, h, radius, 0x35000000);
        rounded(g, x, y, w, h, radius, 0x35F8FBFF);
        rounded(g, x + 1, y + 1, w - 2, h - 2, radius - 1, 0x18FFFFFF);
        g.fill(x + radius, y + 1, x + w - radius, y + 2, 0x70FFFFFF);
        g.fill(x + 1, y + radius, x + 2, y + h - radius, 0x28FFFFFF);
    }

    static void tile(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean hovered) {
        rounded(g, x + 1, y + 4, w, h, 10, 0x26000000);
        int fill = active ? 0x56FFFFFF : hovered ? 0x45FFFFFF : 0x2DFFFFFF;
        rounded(g, x, y, w, h, 10, fill);
        g.fill(x + 12, y + 1, x + w - 12, y + 2, active ? 0xAFFFFFFF : 0x55FFFFFF);
        if (active) {
            g.fill(x + 10, y + h - 3, x + w - 10, y + h - 2, 0xAAB8D0E8);
        }
    }

    static void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        int radius = Math.max(0, Math.min(r, Math.min(w / 2, h / 2)));
        if (radius == 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        g.fill(x + radius, y, x + w - radius, y + h, color);
        g.fill(x, y + radius, x + radius, y + h - radius, color);
        g.fill(x + w - radius, y + radius, x + w, y + h - radius, color);
        for (int row = 0; row < radius; row++) {
            int dy = radius - row - 1;
            int inset = radius - (int) Math.sqrt(Math.max(0, radius * radius - dy * dy));
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }
}
