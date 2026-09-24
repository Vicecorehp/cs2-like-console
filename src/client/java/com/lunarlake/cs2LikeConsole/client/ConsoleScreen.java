package com.lunarlake.cs2LikeConsole.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class ConsoleScreen extends Screen {
    private static final int PADDING = 6;
    private static final int INPUT_HEIGHT = 12;
    private static final float PANEL_HEIGHT_RATIO = 0.6F;
    private static final int BACKDROP_COLOR = 0x66000000;
    private static final int PANEL_COLOR = 0xD9101010;
    private static final int BORDER_COLOR = 0xFF3C3C3C;
    private static final int TEXT_COLOR = 0xFFD4D4D4;
    private static final int PROMPT_COLOR = 0xFF6FC3FF;

    private final List<String> history = new ArrayList<>();
    private final List<FormattedCharSequence> wrapped = new ArrayList<>();

    private EditBox input;
    private int historyIndex;
    private String draft = "";
    private int scroll;
    private int wrappedWidth = -1;
    private int wrappedRevision = -1;
    private boolean bannerLogged;

    public ConsoleScreen() {
        super(Component.translatable("screen.cs2-like-console.console"));
    }

    @Override
    protected void init() {
        if (!bannerLogged) {
            bannerLogged = true;
            ConsoleLog.add(Component.literal("CS2 Like Console").withStyle(ChatFormatting.AQUA));
            ConsoleLog.add(Component.literal("Type a command and press Enter. Press ~ or Esc to close.")
                    .withStyle(ChatFormatting.GRAY));
        }

        historyIndex = history.size();

        int panelHeight = panelHeight();
        int inputY = panelHeight - INPUT_HEIGHT - PADDING;

        input = new EditBox(font, PADDING + font.width("> "), inputY, width - PADDING * 2 - font.width("> "),
                INPUT_HEIGHT, Component.translatable("screen.cs2-like-console.input"));
        input.setMaxLength(256);
        input.setBordered(false);
        addRenderableWidget(input);
        setInitialFocus(input);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        extractor.fill(0, 0, width, height, BACKDROP_COLOR);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        refreshWrapped();

        int panelHeight = panelHeight();
        extractor.fill(0, 0, width, panelHeight, PANEL_COLOR);
        extractor.fill(0, panelHeight - 1, width, panelHeight, BORDER_COLOR);

        extractor.text(font, title, PADDING, PADDING, TEXT_COLOR);

        if (scroll > 0) {
            Component indicator = Component.literal("scrolled up " + scroll);
            extractor.text(font, indicator, width - PADDING - font.width(indicator), PADDING, PROMPT_COLOR);
        }

        int top = logAreaTop();
        int bottom = logAreaBottom();
        int lineHeight = font.lineHeight;
        int rows = Math.max(1, (bottom - top) / lineHeight);
        int end = Math.max(0, wrapped.size() - scroll);
        int start = Math.max(0, end - rows);

        extractor.enableScissor(0, top, width, bottom);

        for (int i = start; i < end; i++) {
            extractor.text(font, wrapped.get(i), PADDING, top + (i - start) * lineHeight, TEXT_COLOR);
        }

        extractor.disableScissor();

        int inputY = panelHeight - INPUT_HEIGHT - PADDING;
        extractor.text(font, ">", PADDING, inputY + (INPUT_HEIGHT - lineHeight) / 2, PROMPT_COLOR);

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();

        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            submit(input.getValue());
            return true;
        }

        if (key == GLFW.GLFW_KEY_UP) {
            navigateHistory(-1);
            return true;
        }

        if (key == GLFW.GLFW_KEY_DOWN) {
            navigateHistory(1);
            return true;
        }

        if (key == GLFW.GLFW_KEY_TAB) {
            complete();
            return true;
        }

        if (key == GLFW.GLFW_KEY_PAGE_UP) {
            scrollBy(visibleRows());
            return true;
        }

        if (key == GLFW.GLFW_KEY_PAGE_DOWN) {
            scrollBy(-visibleRows());
            return true;
        }

        if (key == GLFW.GLFW_KEY_GRAVE_ACCENT) {
            onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0) {
            scrollBy((int) Math.signum(scrollY) * 2);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void submit(String raw) {
        String text = raw.trim();

        if (text.isEmpty()) {
            return;
        }

        if (history.isEmpty() || !history.get(history.size() - 1).equals(text)) {
            history.add(text);
        }

        historyIndex = history.size();
        draft = "";
        input.setValue("");
        scroll = 0;
        ConsoleLog.add(Component.literal("> " + text).withStyle(ChatFormatting.AQUA));

        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().sendCommand(text.startsWith("/") ? text.substring(1) : text);
        }
    }

    private void navigateHistory(int delta) {
        if (history.isEmpty()) {
            return;
        }

        if (historyIndex >= history.size()) {
            draft = input.getValue();
        }

        historyIndex = Mth.clamp(historyIndex + delta, 0, history.size());
        String value = historyIndex >= history.size() ? draft : history.get(historyIndex);
        input.setValue(value);
        input.moveCursorToEnd(false);
    }

    private void complete() {
        String value = input.getValue();

        if (value.isEmpty()) {
            return;
        }

        List<String> matches = new ArrayList<>();

        for (String entry : history) {
            if (entry.startsWith(value) && !matches.contains(entry)) {
                matches.add(entry);
            }
        }

        if (matches.isEmpty()) {
            return;
        }

        int current = matches.indexOf(value);
        String next = matches.get(current < 0 ? 0 : (current + 1) % matches.size());
        input.setValue(next);
        input.moveCursorToEnd(false);
    }

    private void scrollBy(int rows) {
        scroll = Mth.clamp(scroll + rows, 0, Math.max(0, wrapped.size() - visibleRows()));
    }

    private void refreshWrapped() {
        if (wrappedWidth == width && wrappedRevision == ConsoleLog.revision()) {
            return;
        }

        wrappedWidth = width;
        wrappedRevision = ConsoleLog.revision();
        wrapped.clear();

        int maxWidth = width - PADDING * 2;

        for (Component line : ConsoleLog.snapshot()) {
            wrapped.addAll(font.split(line, maxWidth));
        }

        scrollBy(0);
    }

    private int visibleRows() {
        return Math.max(1, (logAreaBottom() - logAreaTop()) / font.lineHeight);
    }

    private int logAreaTop() {
        return PADDING + font.lineHeight + PADDING;
    }

    private int logAreaBottom() {
        return panelHeight() - INPUT_HEIGHT - PADDING * 2;
    }

    private int panelHeight() {
        return Mth.clamp((int) (height * PANEL_HEIGHT_RATIO), INPUT_HEIGHT + PADDING * 4, height);
    }
}
