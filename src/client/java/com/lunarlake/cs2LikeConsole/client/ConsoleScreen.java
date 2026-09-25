package com.lunarlake.cs2LikeConsole.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.chat.ChatAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

public class ConsoleScreen extends Screen {
    private static final int PADDING = 6;
    private static final int INPUT_HEIGHT = 12;
    private static final int INPUT_X = 4;
    private static final String PROMPT = "> ";
    private static final int PANEL_COLOR = 0xC8101010;
    private static final int SEPARATOR_COLOR = 0xFF3C3C3C;
    private static final int TEXT_COLOR = 0xFFD4D4D4;
    private static final int PROMPT_COLOR = 0xFF6FC3FF;
    private static final int SUGGESTION_FILL_COLOR = 0xD0000000;

    private final List<String> history = new ArrayList<>();
    private final List<FormattedCharSequence> wrapped = new ArrayList<>();

    private EditBox input;
    private CommandSuggestions commandSuggestions;
    private String initial = "";
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
            ConsoleLog.add(Component.literal("Type a command and press Tab to complete. Press ~ or Esc to close.")
                    .withStyle(ChatFormatting.GRAY));
        }

        historyIndex = history.size();

        input = new EditBox(font, INPUT_X + font.width(PROMPT), inputY(), width - INPUT_X * 2 - font.width(PROMPT),
                INPUT_HEIGHT, Component.translatable("screen.cs2-like-console.input"));
        input.setMaxLength(256);
        input.setBordered(false);
        input.setCanLoseFocus(false);
        input.setValue(initial);

        commandSuggestions = new CommandSuggestions(minecraft, this, input, font,
                true, false, 1, 10, true, SUGGESTION_FILL_COLOR);
        commandSuggestions.setAllowHiding(false);
        commandSuggestions.setAllowSuggestions(false);

        input.setResponder(text -> {
            commandSuggestions.setAllowSuggestions(!text.isEmpty());

            if (minecraft.player != null && minecraft.getConnection() != null) {
                commandSuggestions.updateCommandInfo();
            }
        });
        addRenderableWidget(input);
        setInitialFocus(input);

        if (minecraft.player != null) {
            ChatAbilities abilities = minecraft.player.chatAbilities();
            commandSuggestions.setRestrictions(abilities.canSendMessages(), abilities.canSendCommands());
        }
    }

    @Override
    public void resize(int width, int height) {
        if (input != null) {
            initial = input.getValue();
        }

        init(width, height);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        refreshWrapped();

        extractor.fill(0, 0, width, height, PANEL_COLOR);
        extractor.text(font, title, PADDING, PADDING, TEXT_COLOR);

        if (scroll > 0) {
            Component indicator = Component.literal("scrolled up " + scroll);
            extractor.text(font, indicator, width - PADDING - font.width(indicator), PADDING, PROMPT_COLOR);
        }

        int top = logAreaTop();
        int bottom = logAreaBottom();
        int rows = visibleRows();
        int end = Math.max(0, wrapped.size() - scroll);
        int start = Math.max(0, end - rows);

        extractor.enableScissor(0, top, width, bottom);

        for (int i = start; i < end; i++) {
            extractor.text(font, wrapped.get(i), PADDING, top + (i - start) * font.lineHeight, TEXT_COLOR);
        }

        extractor.disableScissor();

        extractor.fill(0, inputY() - 3, width, inputY() - 2, SEPARATOR_COLOR);
        extractor.text(font, PROMPT, INPUT_X, inputY() + (INPUT_HEIGHT - font.lineHeight) / 2, PROMPT_COLOR);

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
        commandSuggestions.extractRenderState(extractor, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (commandSuggestions.keyPressed(event)) {
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_GRAVE_ACCENT) {
            onClose();
            return true;
        }

        if (event.isUp()) {
            navigateHistory(-1);
            return true;
        }

        if (event.isDown()) {
            navigateHistory(1);
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_PAGE_UP) {
            scrollBy(visibleRows());
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_PAGE_DOWN) {
            scrollBy(-visibleRows());
            return true;
        }

        if (super.keyPressed(event)) {
            return true;
        }

        if (event.isConfirmation()) {
            if (!commandSuggestions.hasAllowedInput()) {
                return true;
            }

            submit(input.getValue());
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (commandSuggestions.mouseClicked(event)) {
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (commandSuggestions.mouseScrolled(Mth.clamp(scrollY, -1.0, 1.0))) {
            return true;
        }

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

        if (history.isEmpty() || !history.getLast().equals(text)) {
            history.add(text);
        }

        historyIndex = history.size();
        draft = "";
        input.setValue("");
        scroll = 0;
        ConsoleLog.add(Component.literal("> " + text).withStyle(ChatFormatting.AQUA));

        if (minecraft.getConnection() != null) {
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
        input.setValue(historyIndex >= history.size() ? draft : history.get(historyIndex));
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
        return inputY() - PADDING;
    }

    private int inputY() {
        return height - INPUT_HEIGHT;
    }
}
