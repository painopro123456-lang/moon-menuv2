package com.moonmenu;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class ModuleRuntime {
    private static final Set<String> ENABLED = new HashSet<>();
    private static double savedGamma = 1.0;
    private static boolean gammaCaptured;
    private static boolean savedToggleSprint;
    private static boolean savedToggleSneak;
    private static boolean movementKeyForced;

    private ModuleRuntime() {}

    public static void initialize() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("moonmenu", "module_hud"),
                (graphics, deltaTracker) -> renderHud(graphics)
        );
    }

    public static boolean isEnabled(String name) {
        return ENABLED.contains(name);
    }

    public static void toggle(String name) {
        Minecraft client = Minecraft.getInstance();
        if (ENABLED.remove(name)) {
            disable(name, client);
        } else {
            enable(name, client);
        }
    }

    private static void enable(String name, Minecraft client) {
        ENABLED.add(name);
        if (name.equals("Fullbright")) {
            savedGamma = client.options.gamma().get();
            gammaCaptured = true;
            client.options.gamma().set(1.0);
        } else if (name.equals("Toggle Sprint")) {
            savedToggleSprint = client.options.toggleSprint().get();
            client.options.toggleSprint().set(true);
        } else if (name.equals("Toggle Sneak")) {
            savedToggleSneak = client.options.toggleCrouch().get();
            client.options.toggleCrouch().set(true);
        }
    }

    private static void disable(String name, Minecraft client) {
        if (name.equals("Fullbright") && gammaCaptured) {
            client.options.gamma().set(savedGamma);
            gammaCaptured = false;
        } else if (name.equals("Toggle Sprint")) {
            client.options.toggleSprint().set(savedToggleSprint);
        } else if (name.equals("Toggle Sneak")) {
            client.options.toggleCrouch().set(savedToggleSneak);
        } else if (name.equals("Auto Sprint") && client.player != null) {
            client.player.setSprinting(false);
        }
    }

    public static void tick(Minecraft client) {
        if (client.player == null) {
            movementKeyForced = false;
            return;
        }

        if (isEnabled("Auto Sprint")) {
            boolean movingForward = client.options.keyUp.isDown();
            boolean canSprint = movingForward && !client.player.isCrouching()
                    && client.player.getFoodData().getFoodLevel() > 6;
            client.player.setSprinting(canSprint);
            movementKeyForced = true;
        } else if (movementKeyForced) {
            client.player.setSprinting(false);
            movementKeyForced = false;
        }

        if (isEnabled("Fullbright")) {
            client.options.gamma().set(1.0);
        }
    }

    private static void renderHud(net.minecraft.client.gui.GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int y = 8;
        if (isEnabled("FPS")) {
            y = line(graphics, client, "FPS  " + client.getFps(), y);
        }
        if (isEnabled("Coordinates")) {
            y = line(graphics, client, String.format("XYZ  %.1f  %.1f  %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ()), y);
        }
        if (isEnabled("Health Display")) {
            y = line(graphics, client, String.format("HEALTH  %.1f", client.player.getHealth()), y);
        }
        if (isEnabled("Food Status")) {
            y = line(graphics, client, "FOOD  " + client.player.getFoodData().getFoodLevel(), y);
        }
        if (isEnabled("Memory Usage")) {
            Runtime runtime = Runtime.getRuntime();
            long used = (runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L;
            long max = runtime.maxMemory() / 1024L / 1024L;
            y = line(graphics, client, "MEMORY  " + used + " / " + max + " MB", y);
        }
        if (isEnabled("Chunk Coordinates")) {
            int cx = client.player.blockPosition().getX() >> 4;
            int cz = client.player.blockPosition().getZ() >> 4;
            y = line(graphics, client, "CHUNK  " + cx + "  " + cz, y);
        }
        if (isEnabled("Held Item")) {
            String item = client.player.getMainHandItem().isEmpty()
                    ? "Empty" : client.player.getMainHandItem().getHoverName().getString();
            y = line(graphics, client, "HELD  " + item, y);
        }
        if (isEnabled("Durability") || isEnabled("Armor Status")) {
            var stack = client.player.getMainHandItem();
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int remaining = stack.getMaxDamage() - stack.getDamageValue();
                y = line(graphics, client, "DURABILITY  " + remaining + " / " + stack.getMaxDamage(), y);
            }
        }
        if (isEnabled("World Time")) {
            long dayTime = client.level.getLevelData().getDayTime() % 24000L;
            long hours = (dayTime / 1000L + 6L) % 24L;
            long minutes = (dayTime % 1000L) * 60L / 1000L;
            y = line(graphics, client, String.format("WORLD TIME  %02d:%02d", hours, minutes), y);
        }
    }

    private static int line(net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                            Minecraft client, String value, int y) {
        graphics.text(client.font, value, 8, y, 0xFFEAF0FA, true);
        return y + 11;
    }
}
