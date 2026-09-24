package com.lunarlake.cs2LikeConsole.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.minecraft.network.chat.Component;

public final class ConsoleLog {
    private static final int MAX_LINES = 300;
    private static final Deque<Component> LINES = new ArrayDeque<>();
    private static int revision;

    private ConsoleLog() {
    }

    public static void add(Component line) {
        LINES.addLast(line);

        while (LINES.size() > MAX_LINES) {
            LINES.removeFirst();
        }

        revision++;
    }

    public static List<Component> snapshot() {
        return new ArrayList<>(LINES);
    }

    public static int revision() {
        return revision;
    }
}
