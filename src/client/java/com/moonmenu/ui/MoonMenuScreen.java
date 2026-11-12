package com.moonmenu.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

public final class MoonMenuScreen extends Screen {
    private static final String[] GROUPS = {"HUD", "Gameplay", "Visual", "Chat"};
    private final Set<String> enabled = new HashSet<>();
    private String group = "HUD";
    private ModuleSpec selected;
    private int panelX, panelY, panelW, panelH;
    private int contentX, contentY, contentW;
    private int rowW, rowH = 58;

    public MoonMenuScreen() {
        super(Component.literal("Moon Menu"));
    }

    @Override
    protected void init() {
        layout();
    }

    private void layout() {
        panelW = Math.min(790, width - 28);
        panelH = Math.min(440, height - 28);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        contentX = panelX + 258;
        contentY = panelY + 146;
        contentW = panelW - 282;
        rowW = (contentW - 12) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        layout();
        drawAtmosphere(g);
        drawMoonColumn(g);
        drawControls(g, mouseX, mouseY);
        if (selected != null) drawSettings(g, mouseX, mouseY);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    private void drawAtmosphere(GuiGraphicsExtractor g) {
        GlassSurface.rounded(g, panelX + 4, panelY + 8, panelW, panelH, 20, 0x65000000);
        GlassSurface.rounded(g, panelX, panelY, panelW, panelH, 20, 0xC9141B2B);
        GlassSurface.rounded(g, panelX + 1, panelY + 1, panelW - 2, panelH - 2, 19, 0x321D2A40);
        // A restrained rim highlight replaces the old hard vertical divider.
        g.fill(panelX + 22, panelY + 1, panelX + panelW - 22, panelY + 2, 0x48DCE8F7);
        int[][] stars = {{31,34},{92,75},{180,51},{225,103},{44,312},{198,365},{panelW-34,42},{panelW-62,panelH-38}};
        for (int[] star : stars) {
            int sx = panelX + star[0], sy = panelY + star[1];
            GlassSurface.rounded(g, sx, sy, 2, 2, 1, 0x8CC8D8EF);
        }
    }

    private void drawMoonColumn(GuiGraphicsExtractor g) {
        int left = panelX + 26;
        g.text(font, "MOON MENU", left, panelY + 24, 0xFFF0F4FC, false);
        g.text(font, "CLIENT MOD  /  26.2", left, panelY + 42, 0xFF8F9DB4, false);

        int cx = panelX + 116, cy = panelY + 164;
        GlassSurface.rounded(g, cx - 52, cy - 52, 104, 104, 52, 0x1ECCD9EC);
        GlassSurface.rounded(g, cx - 43, cy - 43, 86, 86, 43, 0xDDEAF2FF);
        GlassSurface.rounded(g, cx - 10, cy - 56, 86, 86, 43, 0xF0141B2B);
        GlassSurface.rounded(g, cx + 27, cy - 16, 4, 4, 2, 0xC9DCE8FA);
        GlassSurface.rounded(g, cx - 61, cy + 32, 3, 3, 1, 0xAFC4D5EC);

        g.text(font, group.toUpperCase(), left, panelY + 244, 0xFFE6EDF8, false);
        g.text(font, countForGroup() + " MODULES", left, panelY + 263, 0xFF9AA9BF, false);
        g.fill(left, panelY + 288, panelX + 220, panelY + 289, 0x397F91AA);
        g.text(font, "RIGHT SHIFT", left, panelY + 306, 0xFFD0D9E8, false);
        g.text(font, "TO CLOSE", left, panelY + 322, 0xFF8998AF, false);
        g.text(font, "A LITTLE MORE", left, panelY + panelH - 43, 0xFF8797B0, false);
        g.text(font, "NIGHT IN YOUR HUD", left, panelY + panelH - 27, 0xFF8797B0, false);
    }

    private void drawControls(GuiGraphicsExtractor g, int mx, int my) {
        int x = contentX;
        g.text(font, "THE NIGHT SHIFT", x, panelY + 27, 0xFFF0F4FC, false);
        g.text(font, "Choose what appears in your world.", x, panelY + 47, 0xFF9AA9BF, false);
        g.text(font, "V2.1", panelX + panelW - 54, panelY + 28, 0xFF9EADC1, false);

        int tabX = x, tabY = panelY + 78;
        for (String item : GROUPS) {
            int w = font.width(item) + 24;
            boolean active = group.equals(item);
            boolean hover = inside(mx, my, tabX, tabY, w, 26);
            if (active || hover) {
                GlassSurface.rounded(g, tabX, tabY, w, 26, 9,
                        active ? 0x579EACC6 : 0x2BDAE5F4);
            }
            g.text(font, item, tabX + 12, tabY + 8,
                    active ? 0xFFF4F7FC : 0xFF9AA9BF, false);
            if (active) g.fill(tabX + 12, tabY + 23, tabX + w - 12, tabY + 24, 0xB9DCE8F7);
            tabX += w + 7;
        }

        int index = 0;
        for (ModuleSpec module : ModuleSpec.ALL) {
            if (!module.group().equals(group)) continue;
            int col = index % 2, row = index / 2;
            int rx = contentX + col * (rowW + 12);
            int ry = contentY + row * (rowH + 9);
            index++;
            if (ry + rowH > panelY + panelH - 20) continue;
            boolean on = enabled.contains(module.name());
            boolean hover = inside(mx, my, rx, ry, rowW, rowH);
            GlassSurface.rounded(g, rx, ry + 3, rowW, rowH, 12, 0x30000000);
            GlassSurface.rounded(g, rx, ry, rowW, rowH, 12,
                    on ? 0x4D9AAFCB : hover ? 0x398093B0 : 0x241D2B40);
            if (hover || on) {
                GlassSurface.rounded(g, rx + 1, ry + 1, rowW - 2, 1, 1,
                        on ? 0x70DCE8F7 : 0x35DCE8F7);
            }
            GlassSurface.rounded(g, rx + 12, ry + 12, 32, 32, 11,
                    on ? 0x78DCE8F7 : 0x459AAAC2);
            String glyph = module.glyph();
            g.text(font, glyph, rx + 12 + (32 - font.width(glyph)) / 2,
                    ry + 23, 0xFFE5EDF9, false);
            g.text(font, fit(module.name(), rowW - 91), rx + 53, ry + 15,
                    0xFFEAF0FA, false);
            g.text(font, on ? "ACTIVE" : "INACTIVE", rx + 53, ry + 33,
                    on ? 0xFFD5E3F4 : 0xFF8998AF, false);

            int tx = rx + rowW - 37;
            GlassSurface.rounded(g, tx, ry + 21, 25, 12, 7,
                    on ? 0xB7C9D9F0 : 0x53677891);
            GlassSurface.rounded(g, on ? tx + 14 : tx + 2, ry + 22, 9, 9, 5, 0xFFF3F6FC);
            g.text(font, "···", rx + rowW - 23, ry + 3, 0xFF9EADC1, false);
        }
    }

    private void drawSettings(GuiGraphicsExtractor g, int mx, int my) {
        int w = 248, h = 126;
        int x = panelX + panelW - w - 18;
        int y = panelY + panelH - h - 18;
        GlassSurface.rounded(g, x + 3, y + 5, w, h, 14, 0x90000000);
        GlassSurface.rounded(g, x, y, w, h, 14, 0xF0212B3E);
        g.fill(x + 16, y + 1, x + w - 16, y + 2, 0x55DCE8F7);
        g.text(font, "MODULE DETAILS", x + 15, y + 15, 0xFF9EADC1, false);
        g.text(font, selected.name(), x + 15, y + 36, 0xFFF0F4FC, false);
        g.text(font, fit(selected.description(), w - 30), x + 15, y + 56, 0xFFB2BED0, false);
        g.text(font, "CLICK THE MODULE TO TOGGLE", x + 15, y + 91, 0xFF8998AF, false);
        g.text(font, "×", x + w - 19, y + 11, 0xFFE4EBF6, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mx = event.x(), my = event.y();
        int tabX = contentX, tabY = panelY + 78;
        for (String item : GROUPS) {
            int w = font.width(item) + 24;
            if (inside(mx, my, tabX, tabY, w, 26)) {
                group = item;
                selected = null;
                return true;
            }
            tabX += w + 7;
        }

        int index = 0;
        for (ModuleSpec module : ModuleSpec.ALL) {
            if (!module.group().equals(group)) continue;
            int col = index % 2, row = index / 2;
            int rx = contentX + col * (rowW + 12);
            int ry = contentY + row * (rowH + 9);
            index++;
            if (!inside(mx, my, rx, ry, rowW, rowH)) continue;
            if (mx >= rx + rowW - 30 || my <= ry + 11) {
                selected = module;
            } else if (!enabled.add(module.name())) {
                enabled.remove(module.name());
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
