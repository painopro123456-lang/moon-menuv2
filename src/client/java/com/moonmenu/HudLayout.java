package com.moonmenu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class HudLayout {
    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("moonmenu-hud.properties");

    public record Entry(int x, int y, float scale) {}
    private HudLayout() {}

    public static Entry get(String id, int defaultY) {
        return ENTRIES.computeIfAbsent(id, key -> new Entry(8, defaultY, 1.0f));
    }

    public static void set(String id, int x, int y, float scale) {
        ENTRIES.put(id, new Entry(Math.max(0, x), Math.max(0, y), Math.max(0.5f, Math.min(3.0f, scale))));
        save();
    }

    public static Map<String, Entry> entries() {
        return ENTRIES;
    }

    public static void render(GuiGraphicsExtractor graphics, net.minecraft.client.Minecraft client,
                              String id, String value, int defaultY) {
        Entry entry = get(id, defaultY);
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) entry.x(), (float) entry.y());
        graphics.pose().scale(entry.scale(), entry.scale());
        graphics.text(client.font, value, 0, 0, 0xFFEAF0FA, true);
        graphics.pose().popMatrix();
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties properties = new Properties();
        try (var input = Files.newInputStream(FILE)) {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                String[] parts = properties.getProperty(key).split(",");
                if (parts.length != 3) continue;
                ENTRIES.put(key, new Entry(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                        Float.parseFloat(parts[2])));
            }
        } catch (Exception ignored) {}
    }

    private static void save() {
        Properties properties = new Properties();
        ENTRIES.forEach((key, value) -> properties.setProperty(key,
                value.x() + "," + value.y() + "," + value.scale()));
        try {
            Files.createDirectories(FILE.getParent());
            try (var output = Files.newOutputStream(FILE)) { properties.store(output, "Moon Menu HUD layout"); }
        } catch (IOException ignored) {}
    }
}
