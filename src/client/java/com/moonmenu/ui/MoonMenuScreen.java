package com.moonmenu.ui;

import net.minecraft.client.Minecraft;
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
    private int railW = 142;
    private int cardW, cardH = 112;
    private int gridX, gridY, gridW;
    private int scroll;

    public MoonMenuScreen() {
        super(Component.literal("Moon Menu"));
    }

    @Override
    protected void init() {
        panelW = Math.min(820, width - 36);
        panelH = Math.min(510, height - 36);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        gridX = panelX + railW + 24;
        gridY = panelY + 132;
        gridW = panelW - railW - 48;
        cardW = (gridW - 3 * 12) / 4;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        panelW = Math.min(820, width - 36);
        panelH = Math.min(510, height - 36);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        gridX = panelX + railW + 24;
        gridY = panelY + 132;
        gridW = panelW - railW - 48;
        cardW = (gridW - 3 * 12) / 4;

        GlassSurface.panel(g, panelX, panelY, panelW, panelH, 18);
        drawRail(g, mouseX, mouseY);
        drawHeader(g);
        drawModules(g, mouseX, mouseY);
        drawFooter(g);
        if (selected != null) drawDetails(g, mouseX, mouseY);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    private void drawRail(GuiGraphicsExtractor g, int mx, int my) {
        int x = panelX + 18;
        int y = panelY + 20;
        GlassSurface.rounded(g, x, y, 34, 34, 11, 0x58FFFFFF);
        GlassSurface.rounded(g, x + 8, y + 8, 18, 18, 6, 0xB9FFFFFF);
        g.text(font, "M", x + 13, y + 12, 0xFF34475C, false);
        g.text(font, "MOON", x + 43, y + 5, 0xFF172638, false);
        g.text(font, "MENU  /  02", x + 43, y + 20, 0xFF637386, false);

        g.fill(panelX + railW, panelY + 18, panelX + railW + 1, panelY + panelH - 18, 0x30FFFFFF);
        g.text(font, "LIBRARY", x + 2, panelY + 82, 0xFF7C8997, false);
        int navY = panelY + 105;
        for (String item : GROUPS) {
            boolean active = group.equals(item);
            boolean hover = inside(mx, my, x - 7, navY - 6, railW - 28, 32);
            if (active || hover) GlassSurface.rounded(g, x - 7, navY - 6, railW - 28, 32, 9,
                    active ? 0x65FFFFFF : 0x28FFFFFF);
            g.text(font, item, x + 5, navY + 5, active ? 0xFF26384B : 0xFF647386, false);
            navY += 39;
        }

        int bottom = panelY + panelH - 54;
        g.fill(x, bottom - 10, panelX + railW - 18, bottom - 9, 0x32FFFFFF);
        g.text(font, "MOON MENU", x + 2, bottom + 2, 0xFF667689, false);
        g.text(font, "FABRIC 26.2", x + 2, bottom + 17, 0xFF8B98A5, false);
    }

    private void drawHeader(GuiGraphicsExtractor g) {
        int x = gridX;
        g.text(font, "YOUR MODULES", x, panelY + 42, 0xFF1D2B3A, false);
        g.text(font, "A quieter interface. Your game stays in view.", x, panelY + 62, 0xFF667789, false);
        String count = countForGroup() + " ITEMS";
        g.text(font, count, panelX + panelW - 112 - font.width(count), panelY + 46, 0xFF758496, false);
        GlassSurface.rounded(g, panelX + panelW - 92, panelY + 75, 68, 24, 8, 0x38FFFFFF);
        g.text(font, "SEARCH", panelX + panelW - 75, panelY + 83, 0xFF718093, false);
    }

    private void drawModules(GuiGraphicsExtractor g, int mx, int my) {
        int index = 0;
        for (ModuleSpec module : ModuleSpec.ALL) {
            if (!module.group().equals(group)) continue;
            int col = index % 4;
            int row = index / 4;
            int x = gridX + col * (cardW + 12);
            int y = gridY + row * (cardH + 12) - scroll;
            index++;
            if (y + cardH < gridY || y > panelY + panelH - 50) continue;
            boolean on = enabled.contains(module.name());
            boolean hover = inside(mx, my, x, y, cardW, cardH);
            GlassSurface.tile(g, x, y, cardW, cardH, on, hover);

            int iconX = x + (cardW - 38) / 2;
            GlassSurface.rounded(g, iconX, y + 13, 38, 38, 12,
                    on ? 0x80FFFFFF : 0x4AFFFFFF);
            String glyph = module.glyph();
            g.text(font, glyph, iconX + (38 - font.width(glyph)) / 2, y + 25, 0xFF40546A, false);

            String name = fit(module.name(), cardW - 14);
            g.text(font, name, x + (cardW - font.width(name)) / 2, y + 60, 0xFF243448, false);
            String state = on ? "ENABLED" : "DISABLED";
            g.text(font, state, x + (cardW - font.width(state)) / 2, y + 77,
                    on ? 0xFF506C89 : 0xFF8491A0, false);

            int gearX = x + cardW - 22;
            GlassSurface.rounded(g, gearX, y + 8, 16, 16, 5, 0x36FFFFFF);
            g.text(font, "·", gearX + 6, y + 10, 0xFF647589, false);
        }
    }

    private void drawDetails(GuiGraphicsExtractor g, int mx, int my) {
        int w = 260, h = 154;
        int x = panelX + panelW - w - 22;
        int y = panelY + panelH - h - 24;
        GlassSurface.panel(g, x, y, w, h, 14);
        g.text(font, "MODULE SETTINGS", x + 16, y + 17, 0xFF718093, false);
        g.text(font, selected.name(), x + 16, y + 38, 0xFF1E2D3E, false);
        g.text(font, fit(selected.description(), w - 32), x + 16, y + 58, 0xFF647588, false);
        GlassSurface.rounded(g, x + 16, y + 91, w - 32, 32, 9, 0x55FFFFFF);
        g.text(font, "Toggle this module from its card", x + 27, y + 102, 0xFF4D6074, false);
        g.text(font, "×", x + w - 23, y + 12, 0xFF526579, false);
    }

    private void drawFooter(GuiGraphicsExtractor g) {
        int y = panelY + panelH - 31;
        g.fill(gridX, y - 7, panelX + panelW - 20, y - 6, 0x30FFFFFF);
        g.text(font, "RIGHT SHIFT  ·  CLOSE", gridX, y + 2, 0xFF8491A0, false);
        g.text(font, "MOON MENU  /  NEW INTERFACE", panelX + panelW - 202, y + 2, 0xFF8491A0, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mx = event.x();
        double my = event.y();
        int x = panelX + 18;
        int navY = panelY + 105;
        for (String item : GROUPS) {
            if (inside(mx, my, x - 7, navY - 6, railW - 28, 32)) {
                group = item;
                scroll = 0;
                selected = null;
                return true;
            }
            navY += 39;
        }

        int index = 0;
        for (ModuleSpec module : ModuleSpec.ALL) {
            if (!module.group().equals(group)) continue;
            int col = index % 4, row = index / 4;
            int cx = gridX + col * (cardW + 12);
            int cy = gridY + row * (cardH + 12) - scroll;
            index++;
            if (!inside(mx, my, cx, cy, cardW, cardH)) continue;
            if (mx >= cx + cardW - 26 && my <= cy + 30) {
                selected = module;
            } else if (!enabled.add(module.name())) {
                enabled.remove(module.name());
            }
            return true;
        }

        if (selected != null) {
            int w = 260, h = 154;
            int dx = panelX + panelW - w - 22;
            int dy = panelY + panelH - h - 24;
            if (inside(mx, my, dx + w - 30, dy, 30, 30)) {
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
        while (!result.isEmpty() && font.width(result + "…") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
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
