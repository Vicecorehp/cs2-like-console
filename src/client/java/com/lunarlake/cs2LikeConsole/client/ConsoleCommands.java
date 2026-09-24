package com.lunarlake.cs2LikeConsole.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

public final class ConsoleCommands {
    private ConsoleCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("disconnect")
                    .requires(FabricClientCommandSource::attended)
                    .executes(context -> disconnect()));

            dispatcher.register(ClientCommands.literal("exit")
                    .requires(FabricClientCommandSource::attended)
                    .executes(context -> quit()));

            dispatcher.register(ClientCommands.literal("quit")
                    .requires(FabricClientCommandSource::attended)
                    .executes(context -> quit()));
        });
    }

    private static int disconnect() {
        Minecraft minecraft = Minecraft.getInstance();
        ConsoleLog.add(Component.translatable("command.cs2-like-console.disconnecting")
                .withStyle(ChatFormatting.YELLOW));
        minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
        minecraft.gui.setScreen(new TitleScreen());
        return 1;
    }

    private static int quit() {
        Minecraft.getInstance().stop();
        return 1;
    }
}
