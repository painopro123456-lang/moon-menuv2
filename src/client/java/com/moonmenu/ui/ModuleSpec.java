package com.moonmenu.ui;

import java.util.List;

public record ModuleSpec(String name, String description, String group, String glyph) {
    public static final List<ModuleSpec> ALL = List.of(
            new ModuleSpec("Armor Status", "Equipment durability at a glance", "HUD", "A"),
            new ModuleSpec("Potion Effects", "Active effects and remaining time", "HUD", "P"),
            new ModuleSpec("Keystrokes", "Show movement and action keys", "HUD", "K"),
            new ModuleSpec("CPS", "Live clicks per second", "HUD", "C"),
            new ModuleSpec("FPS", "Display current frame rate", "HUD", "F"),
            new ModuleSpec("Coordinates", "Player position and dimension", "HUD", "⌖"),
            new ModuleSpec("Direction", "Compass heading and facing", "HUD", "N"),
            new ModuleSpec("Ping", "Connection latency display", "HUD", "↗"),
            new ModuleSpec("Clock", "Local time overlay", "HUD", "◷"),
            new ModuleSpec("Speed", "Current movement speed", "HUD", "S"),
            new ModuleSpec("Zoom", "Hold-to-zoom camera control", "Gameplay", "Z"),
            new ModuleSpec("Auto Sprint", "Sprint while moving forward", "Gameplay", "↟"),
            new ModuleSpec("Fullbright", "Increase visibility in dark areas", "Visual", "☼"),
            new ModuleSpec("Item Alerts", "Notify when selected items change", "Visual", "◇"),
            new ModuleSpec("Chat Timestamps", "Add time markers to chat", "Chat", "◷"),
            new ModuleSpec("Compact Chat", "Reduce chat visual footprint", "Chat", "≡")
    );

    private ModuleSpec() {
        this("", "", "", "");
    }
}
