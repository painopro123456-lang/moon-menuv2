package com.moonmenu;

import com.moonmenu.ui.MoonMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class MoonMenuClient implements ClientModInitializer {
    private static KeyMapping openMenu;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("moonmenu", "controls"));
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.moonmenu.open",
                net.minecraft.client.input.KeyEvent.Type.KEYSYM == null
                        ? null : com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) {
                if (client.screen == null) {
                    client.setScreenAndShow(new MoonMenuScreen());
                } else if (client.screen instanceof MoonMenuScreen) {
                    client.setScreen(null);
                }
            }
        });
    }
}
