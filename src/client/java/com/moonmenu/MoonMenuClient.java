package com.moonmenu;

import com.moonmenu.ui.MoonMenuScreen;
import com.moonmenu.ui.HudEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class MoonMenuClient implements ClientModInitializer {
    private static KeyMapping openMenu;
    private static KeyMapping openHudEditor;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("moonmenu", "controls"));
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.moonmenu.open",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category));

        openHudEditor = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.moonmenu.hud_editor",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                category));

        ModuleRuntime.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleRuntime.tick(client);
            while (openHudEditor.consumeClick()) {
                if (client.gui.screen() == null) client.setScreenAndShow(new HudEditorScreen());
                else if (client.gui.screen() instanceof HudEditorScreen) client.gui.setScreen(null);
            }
            while (openMenu.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.setScreenAndShow(new MoonMenuScreen());
                } else if (client.gui.screen() instanceof MoonMenuScreen) {
                    client.gui.setScreen(null);
                }
            }
        });
    }
}
