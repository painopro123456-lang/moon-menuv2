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
    private int rowW, rowH = 22;\n    private static final int PAGE_SIZE = 9;\n    private int page = 0;

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
        rowW = contentW;
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
        // Layered atmospheric halo: soft, wide rings behind the moon.
        GlassSurface.rounded(g, cx - 66, cy - 66, 132, 132, 66, 0x102E4664);
        GlassSurface.rounded(g, cx - 58, cy - 58, 116, 116, 58, 0x183D5878);
        GlassSurface.rounded(g, cx - 51, cy - 51, 102, 102, 51, 0x286C829F);
        // A fine outer rim and cool pearl surface.
        GlassSurface.rounded(g, cx - 44, cy - 44, 88, 88, 44, 0x6CDAE7F8);
        GlassSurface.rounded(g, cx - 40, cy - 40, 80, 80, 40, 0xFFE9F0FA);
        // Offset shadow carves a clean crescent, leaving the illuminated
        // silver edge on the left rather than a flat circle.
        GlassSurface.rounded(g, cx - 4, cy - 51, 82, 82, 41, 0xF0141B2B);
        // Delicate surface marks on the lit arc.
        GlassSurface.rounded(g, cx - 29, cy - 16, 8, 8, 4, 0x2D8195AE);
        GlassSurface.rounded(g, cx - 19, cy + 10, 5, 5, 3, 0x26788DA8);
        GlassSurface.rounded(g, cx - 34, cy + 25, 3, 3, 2, 0x33788DA8);
        // Tiny distant stars complete the moonlit focal point.
        GlassSurface.rounded(g, cx + 53, cy - 37, 3, 3, 2, 0xD7E6F2FF);
        GlassSurface.rounded(g, cx + 61, cy + 16, 2, 2, 1, 0xAFC4D5EC);
        GlassSurface.rounded(g, cx - 61, cy - 49, 2, 2, 1, 0xAFC4D5EC);
        GlassSurface.rounded(g, cx - 58, cy + 43, 3, 3, 2, 0xAFC4D5EC);

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
            if (index < page * PAGE_SIZE || index >= (page + 1) * PAGE_SIZE) {
                index++;
                continue;
            }
            int visibleIndex = index - page * PAGE_SIZE;
            int rx = contentX;
            int ry = contentY + visibleIndex * (rowH + 5);
            index++;
            if (ry + rowH > panelY + panelH - 34) continue;
            boolean on = ModuleRuntime.isEnabled(module.name());
            boolean hover = inside(mx, my, rx, ry, rowW, rowH);
            if (hover || on) {
                GlassSurface.rounded(g, rx, ry, rowW, rowH, 7,
                        on ? 0x4D9AAFCB : 0x328093B0);
            } else {
                GlassSurface.rounded(g, rx, ry, rowW, rowH, 7, 0x1E9AAAC2);
            }
            g.text(font, fit(module.name(), rowW - 112), rx + 12, ry + 7,
                    0xFFEAF0FA, false);
            g.text(font, on ? "ON" : "OFF", rx + rowW - 79, ry + 7,
                    on ? 0xFFDCE8F7 : 0xFF8998AF, false);
            int tx = rx + rowW - 38;
            GlassSurface.rounded(g, tx, ry + 5, 25, 12, 7,
                    on ? 0xB7C9D9F0 : 0x53677891);
            GlassSurface.rounded(g, on ? tx + 14 : tx + 2, ry + 6, 9, 9, 5, 0xFFF3F6FC);
            g.text(font, "···", rx + rowW - 104, ry + 5, 0xFF9EADC1, false);
        }
        int pages = Math.max(1, (countForGroup() + PAGE_SIZE - 1) / PAGE_SIZE);
        int footerY = panelY + panelH - 24;
        g.text(font, "PAGE " + (page + 1) + " / " + pages, contentX, footerY,
                0xFF9AA9BF, false);
        g.text(font, "‹", contentX + contentW - 42, footerY - 3,
                page > 0 ? 0xFFEAF0FA : 0xFF59677D, false);
        g.text(font, "›", contentX + contentW - 17, footerY - 3,
                page + 1 < pages ? 0xFFEAF0FA : 0xFF59677D, false);
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
                page = 0;
                selected = null;
                return true;
            }
            tabX += w + 7;
        }

        int pages = Math.max(1, (countForGroup() + PAGE_SIZE - 1) / PAGE_SIZE);
        int footerY = panelY + panelH - 30;
        if (inside(mx, my, contentX + contentW - 52, footerY, 24, 28) && page > 0) {
            page--;
            selected = null;
            return true;
        }
        if (inside(mx, my, contentX + contentW - 27, footerY, 24, 28) && page + 1 < pages) {
            page++;
            selected = null;
            return true;
        }

        int index = 0;
        for (ModuleSpec module : ModuleSpec.ALL) {
            if (!module.group().equals(group)) continue;
            if (index < page * PAGE_SIZE || index >= (page + 1) * PAGE_SIZE) {
                index++;
                continue;
            }
            int visibleIndex = index - page * PAGE_SIZE;
            int rx = contentX;
            int ry = contentY + visibleIndex * (rowH + 5);
            index++;
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
