package com.moonmenu.ui;

import com.moonmenu.ModuleRuntime;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;


public final class MoonMenuScreen extends Screen {
    private static final String[] GROUPS = {"HUD", "Gameplay", "Visual", "Chat"};
    private String group = "HUD";
    private ModuleSpec selected;
    private int panelX, panelY, panelW, panelH;
    private int contentX, contentY, contentW;
    private int rowW, rowH = 32;
    private int scrollOffset = 0;
    private int visibleRows;

    public MoonMenuScreen() {
        super(Component.literal("Moon Menu"));
    }

    @Override
    protected void init() {
        layout();
    }

    private void layout() {
        panelW = Math.min(900, width - 28);
        panelH = Math.min(500, height - 28);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        contentX = panelX + 176;
        contentY = panelY + 112;
        contentW = panelW - 198;
        rowW = contentW;
        visibleRows = Math.max(1, (panelY + panelH - 48 - contentY) / (rowH + 5));
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, countForGroup() - visibleRows)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        layout();
        drawAtmosphere(g);
        drawSidebar(g);
        drawColorPicker(g);
        drawControls(g, mouseX, mouseY);
        if (selected != null) drawSettings(g, mouseX, mouseY);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    private void drawAtmosphere(GuiGraphicsExtractor g) {
        GlassSurface.rounded(g, panelX + 4, panelY + 8, panelW, panelH, 20, 0x65000000);
        GlassSurface.rounded(g, panelX, panelY, panelW, panelH, 20, MenuTheme.tint(0xC9141B2B, 0.28f));
        GlassSurface.rounded(g, panelX + 1, panelY + 1, panelW - 2, panelH - 2, 19, MenuTheme.tint(0x321D2A40, 0.38f));
        // A restrained rim highlight replaces the old hard vertical divider.
        g.fill(panelX + 22, panelY + 1, panelX + panelW - 22, panelY + 2, MenuTheme.accentWithAlpha(0x90));
        int[][] stars = {{31,34},{92,75},{180,51},{225,103},{44,312},{198,365},{panelW-34,42},{panelW-62,panelH-38}};
        for (int[] star : stars) {
            int sx = panelX + star[0], sy = panelY + star[1];
            GlassSurface.rounded(g, sx, sy, 2, 2, 1, 0x8CC8D8EF);
        }
    }

    private void drawSidebar(GuiGraphicsExtractor g) {
        int left = panelX + 22;
        int sidebarRight = panelX + 156;
        g.text(font, "MOON MENU", left, panelY + 22, MenuTheme.accent(), false);
        g.text(font, "PVP CLIENT", left, panelY + 39, 0xFF8F9DB4, false);
        g.fill(left, panelY + 58, sidebarRight, panelY + 59, 0x397F91AA);

        int y = panelY + 76;
        for (String item : GROUPS) {
            boolean active = group.equals(item);
            int h = 30;
            if (active) {
                GlassSurface.rounded(g, left - 5, y - 5, 137, h, 7,
                        MenuTheme.accentWithAlpha(0x50));
                GlassSurface.rounded(g, left - 5, y + 1, 2, h - 12, 1,
                        MenuTheme.accentWithAlpha(0xE0));
            }
            g.text(font, item, left + 7, y + 5,
                    active ? MenuTheme.accent() : 0xFF9AA9BF, false);
            y += 36;
        }

        g.text(font, ModuleSpec.ALL.size() + " TOTAL MODULES", left, panelY + 236,
                0xFF9AA9BF, false);
        g.text(font, group + " • " + countForGroup(), left, panelY + 250,
                0xFF71839D, false);
        g.fill(left, panelY + 256, sidebarRight, panelY + 257, 0x397F91AA);
        g.text(font, "MENU COLOR", left, panelY + panelH - 67, 0xFFDCE5F2, false);
        g.text(font, "CHANGE ACCENT", left, panelY + panelH - 51, 0xFF8797B0, false);
    }

    private void drawColorPicker(GuiGraphicsExtractor g) {
        int x = panelX + 22;
        int y = panelY + panelH - 35;
        int w = 132, h = 9;
        int segments = 72;
        for (int i = 0; i < segments; i++) {
            int left = x + i * w / segments;
            int right = x + (i + 1) * w / segments;
            float hue = i / (float) (segments - 1);
            g.fill(left, y, right, y + h, hsv(hue, 1.0f, 1.0f));
        }
        int marker = x + Math.round(MenuTheme.hueFraction() * (w - 1));
        GlassSurface.rounded(g, marker - 3, y - 3, 7, h + 6, 3, 0xFFFFFFFF);
        GlassSurface.rounded(g, marker - 1, y - 1, 3, h + 2, 1, MenuTheme.accent());
    }

    private static int hsv(float hue, float saturation, float value) {
        float h = (hue - (float) Math.floor(hue)) * 6.0f;
        int sector = (int) h;
        float fraction = h - sector;
        float p = value * (1.0f - saturation);
        float q = value * (1.0f - fraction * saturation);
        float t = value * (1.0f - (1.0f - fraction) * saturation);
        float red, green, blue;
        switch (sector % 6) {
            case 0 -> { red = value; green = t; blue = p; }
            case 1 -> { red = q; green = value; blue = p; }
            case 2 -> { red = p; green = value; blue = t; }
            case 3 -> { red = p; green = q; blue = value; }
            case 4 -> { red = t; green = p; blue = value; }
            default -> { red = value; green = p; blue = q; }
        }
        return 0xFF000000 | ((int) (red * 255) << 16)
                | ((int) (green * 255) << 8) | (int) (blue * 255);
    }

    private void drawControls(GuiGraphicsExtractor g, int mx, int my) {
        int x = contentX;
        g.text(font, group.toUpperCase(), x, panelY + 24, 0xFFF0F4FC, false);
        g.text(font, "A clean glass view of your modules.", x, panelY + 43, 0xFF9AA9BF, false);
        g.text(font, "MOON MENU", panelX + panelW - 93, panelY + 28, 0xFF9EADC1, false);

        

        int index = 0;
        int shown = 0;
        for (ModuleSpec module : ModuleSpec.ALL) {
            if (!module.group().equals(group)) continue;
            if (index++ < scrollOffset) continue;
            if (shown >= visibleRows) break;
            int rx = contentX;
            int ry = contentY + shown * (rowH + 5);
            shown++;
            boolean on = ModuleRuntime.isEnabled(module.name());
            boolean hover = inside(mx, my, rx, ry, rowW, rowH);
            if (hover || on) {
                GlassSurface.rounded(g, rx + 2, ry + 2, rowW - 4, rowH - 4, 7,
                        on ? MenuTheme.accentWithAlpha(0x70) : MenuTheme.tint(0x328093B0, 0.22f));
            } else {
                GlassSurface.rounded(g, rx + 2, ry + 2, rowW - 4, rowH - 4, 7, MenuTheme.tint(0x1E9AAAC2, 0.18f));
            }
            g.text(font, fit(module.name(), rowW - 160), rx + 13, ry + 3,
                    0xFFEAF0FA, false);
            g.text(font, fit(module.description(), rowW - 160), rx + 13, ry + 16,
                    0xFF9AA9BF, false);
            g.text(font, on ? "ON" : "OFF", rx + rowW - 79, ry + 9,
                    on ? 0xFFDCE8F7 : 0xFF8998AF, false);
            int tx = rx + rowW - 38;
            GlassSurface.rounded(g, tx, ry + 8, 25, 12, 7,
                    on ? MenuTheme.accentWithAlpha(0xD0) : 0x53677891);
            GlassSurface.rounded(g, on ? tx + 14 : tx + 2, ry + 9, 9, 9, 5, 0xFFF3F6FC);
            g.text(font, "···", rx + rowW - 104, ry + 6, 0xFF9EADC1, false);
        }
        int total = countForGroup();
        g.text(font, total + " " + group.toUpperCase() + " • " + ModuleSpec.ALL.size() + " TOTAL", contentX, panelY + panelH - 25,
                0xFF9AA9BF, false);
        if (scrollOffset > 0) {
            g.text(font, "↑", contentX + contentW - 42, panelY + 90, 0xFFEAF0FA, false);
        }
        if (scrollOffset + visibleRows < total) {
            g.text(font, "↓", contentX + contentW - 42, panelY + panelH - 54, 0xFFEAF0FA, false);
        }
    }

    private void drawSettings(GuiGraphicsExtractor g, int mx, int my) {
        int w = 248, h = 126;
        int x = panelX + panelW - w - 18;
        int y = panelY + panelH - h - 18;
        GlassSurface.rounded(g, x + 3, y + 5, w, h, 14, 0x90000000);
        GlassSurface.rounded(g, x, y, w, h, 14, MenuTheme.tint(0xF0212B3E, 0.32f));
        g.fill(x + 16, y + 1, x + w - 16, y + 2, MenuTheme.accentWithAlpha(0xD0));
        g.text(font, "MODULE DETAILS", x + 15, y + 15, MenuTheme.accent(), false);
        g.text(font, selected.name(), x + 15, y + 36, 0xFFF0F4FC, false);
        g.text(font, fit(selected.description(), w - 30), x + 15, y + 56, 0xFFB2BED0, false);
        g.text(font, "CLICK THE MODULE TO TOGGLE", x + 15, y + 91, 0xFF8998AF, false);
        g.text(font, "×", x + w - 19, y + 11, 0xFFE4EBF6, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        layout();
        double mx = event.x(), my = event.y();

                int pickerX = panelX + 22, pickerY = panelY + panelH - 35;
        if (inside(mx, my, pickerX - 5, pickerY - 7, 142, 23)) {
            updateAccentFromPointer(mx, pickerX);
            return true;
        }
        int navY = panelY + 76;
        for (String item : GROUPS) {
            if (inside(mx, my, panelX + 17, navY - 5, 137, 30)) {
                group = item;
                scrollOffset = 0;
                selected = null;
                return true;
            }
            navY += 36;
        }

        int index = 0;
        for (ModuleSpec module : ModuleSpec.ALL) {
            if (!module.group().equals(group)) continue;
            if (index++ < scrollOffset) continue;
            int visibleIndex = index - 1 - scrollOffset;
            if (visibleIndex >= visibleRows) break;
            int rx = contentX;
            int ry = contentY + visibleIndex * (rowH + 5);
            if (!inside(mx, my, rx, ry, rowW, rowH)) continue;
            if (mx >= rx + rowW - 112 && mx < rx + rowW - 82) {
                selected = module;
            } else {
                ModuleRuntime.toggle(module.name());
            }
            return true;
        }

        if (selected != null) {
            int w = 248, h = 126;
            int x = panelX + panelW - w - 18, y = panelY + panelH - h - 18;
            if (inside(mx, my, x + w - 28, y, 28, 28)) {
                selected = null;
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int pickerX = panelX + 22, pickerY = panelY + panelH - 35;
        if (inside(mouseX, mouseY, pickerX - 5, pickerY - 7, 142, 23)) {
            updateAccentFromPointer(mouseX, pickerX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateAccentFromPointer(double mouseX, int pickerX) {
        float hue = (float) Math.max(0.0, Math.min(1.0, (mouseX - pickerX) / 132.0));
        MenuTheme.setAccent(hsv(hue, 1.0f, 1.0f));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int max = Math.max(0, countForGroup() - visibleRows);
        if (verticalAmount < 0) scrollOffset = Math.min(max, scrollOffset + 3);
        else if (verticalAmount > 0) scrollOffset = Math.max(0, scrollOffset - 3);
        return true;
    }

    private int countForGroup() {
        int n = 0;
        for (ModuleSpec module : ModuleSpec.ALL) if (module.group().equals(group)) n++;
        return n;
    }

    private String fit(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) return value;
        String result = value;
        while (!result.isEmpty() && font.width(result + "…") > maxWidth) result = result.substring(0, result.length() - 1);
        return result + "…";
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
