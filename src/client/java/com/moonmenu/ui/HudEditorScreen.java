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
            g.pose().pushMatrix();
            g.pose().translate((float) e.x(), (float) e.y());
            g.pose().scale(e.scale(), e.scale());
            g.fill(-5, -4, w, h, 0x553B6B9C);
            g.fill(-5, -4, w, -3, 0xFF9BCBFF);
            g.text(font, label + "  " + Math.round(e.scale() * 100) + "%", 0, 0,
                    0xFFFFFFFF, true);
            g.pose().popMatrix();
        }
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mx = event.x(), my = event.y();
        for (Map.Entry<String, HudLayout.Entry> item : HudLayout.entries().entrySet()) {
            HudLayout.Entry e = item.getValue();
            int w = Math.max(90, font.width(item.getKey()) + 18);
            int scaledW = Math.round((w + 5) * e.scale());
            int scaledH = Math.round(24 * e.scale());
            if (mx >= e.x() - 5 * e.scale() && mx <= e.x() + scaledW
                    && my >= e.y() - 4 * e.scale() && my <= e.y() + scaledH) {
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
            int scaledW = Math.round((w + 5) * e.scale());
            int scaledH = Math.round(24 * e.scale());
            if (mouseX >= e.x() - 5 * e.scale() && mouseX <= e.x() + scaledW
                    && mouseY >= e.y() - 4 * e.scale() && mouseY <= e.y() + scaledH) {
                HudLayout.set(item.getKey(), e.x(), e.y(), e.scale() + (verticalAmount > 0 ? 0.1f : -0.1f));
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
