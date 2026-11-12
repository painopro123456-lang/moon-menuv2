package com.moonmenu.ui;

import com.moonmenu.HudLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HudEditorScreen extends Screen {
    private String dragging;
    private double grabX, grabY;
    private final List<String> names = new ArrayList<>();

    public HudEditorScreen() {
        super(Component.literal("Moon Menu HUD Editor"));
    }

    @Override
    protected void init() {
        names.clear();
        names.addAll(HudLayout.entries().keySet());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x55000000);
        g.text(font, "HUD EDITOR  •  DRAG TO MOVE  •  SCROLL TO SCALE  •  ESC TO CLOSE",
                12, 12, 0xFFFFFFFF, true);
        for (Map.Entry<String, HudLayout.Entry> item : HudLayout.entries().entrySet()) {
            HudLayout.Entry e = item.getValue();
            String label = item.getKey();
            int w = Math.max(90, font.width(label) + 18);
            int h = 20;
            g.fill(e.x() - 5, e.y() - 4, e.x() + w, e.y() + h, 0x553B6B9C);
            g.fill(e.x() - 5, e.y() - 4, e.x() + w, e.y() - 3, 0xFF9BCBFF);
            g.text(font, label + "  " + Math.round(e.scale() * 100) + "%", e.x(), e.y(),
                    0xFFFFFFFF, true);
        }
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mx = event.x(), my = event.y();
        for (Map.Entry<String, HudLayout.Entry> item : HudLayout.entries().entrySet()) {
            HudLayout.Entry e = item.getValue();
            int w = Math.max(90, font.width(item.getKey()) + 18);
            if (mx >= e.x() - 5 && mx <= e.x() + w && my >= e.y() - 4 && my <= e.y() + 20) {
                dragging = item.getKey();
                grabX = mx - e.x();
                grabY = my - e.y();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            HudLayout.Entry old = HudLayout.get(dragging, 8);
            HudLayout.set(dragging, (int)(event.x() - grabX), (int)(event.y() - grabY), old.scale());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Map.Entry<String, HudLayout.Entry> item : HudLayout.entries().entrySet()) {
            HudLayout.Entry e = item.getValue();
            int w = Math.max(90, font.width(item.getKey()) + 18);
            if (mouseX >= e.x() - 5 && mouseX <= e.x() + w && mouseY >= e.y() - 4 && mouseY <= e.y() + 20) {
                HudLayout.set(item.getKey(), e.x(), e.y(), e.scale() + (verticalAmount > 0 ? 0.1f : -0.1f));
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
