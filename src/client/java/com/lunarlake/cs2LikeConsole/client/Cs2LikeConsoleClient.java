package com.lunarlake.cs2LikeConsole.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cs2LikeConsoleClient implements ClientModInitializer {
    public static final String MOD_ID = "cs2-like-console";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyMapping.Category consoleCategory;
    public static KeyMapping openConsole;

    @Override
    public void onInitializeClient() {
        consoleCategory = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "console"));

        openConsole = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key." + MOD_ID + ".open_console",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                consoleCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConsole.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.setScreenAndShow(new ConsoleScreen());
                }
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> ConsoleLog.add(message));
        ClientReceiveMessageEvents.CHAT.register(
                (message, signedMessage, sender, boundChatType, timestamp) -> ConsoleLog.add(message));

        LOGGER.info("Console key mapping registered");
    }
}
