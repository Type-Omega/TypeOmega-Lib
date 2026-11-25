package com.typeomega.lib.ui.tool;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TabButton;
import com.lowdragmc.lowdraglib.gui.widget.TabContainer;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * A tool window in the GTNH look (GTIA-D-435): the GTCEu bevelled grey, a row of tabs on top and one page per tab.
 * Used by held-item tools such as the Nexus Terminal; the page widgets come from the mod, the frame and the small
 * controls (toggles, number fields, buttons) come from here so every Type Omega tool looks the same.
 */
public final class ToolWindow {

    public static final int TAB_WIDTH = 72;
    public static final int TAB_HEIGHT = 18;
    public static final int TEXT_COLOUR = 0xFF2B2B2B;
    public static final int PAD = 6;

    private final int width, height;
    private final WidgetGroup root;
    private final TabContainer tabs;
    private int tabCount;

    public ToolWindow(int width, int height) {
        this.width = width;
        this.height = height;
        this.root = new WidgetGroup(0, 0, width, height + TAB_HEIGHT);
        this.tabs = new TabContainer(0, 0, width, height + TAB_HEIGHT);
        root.addWidget(new ImageWidget(0, TAB_HEIGHT - 2, width, height + 2, GuiTextures.BACKGROUND));
        root.addWidget(tabs);
    }

    /** Size of the area a page gets, inside the frame. */
    public int pageWidth() {
        return width - 2 * PAD;
    }

    public int pageHeight() {
        return height - 2 * PAD;
    }

    /** Adds a tab with its title; the page is placed inside the frame and shown when the tab is picked. */
    public ToolWindow tab(Component title, WidgetGroup page) {
        int x = tabCount++ * (TAB_WIDTH + 2);
        var button = new TabButton(x, 0, TAB_WIDTH, TAB_HEIGHT);
        var text = new TextTexture(title.getString()).setWidth(TAB_WIDTH - 4).setType(TextTexture.TextType.ROLL);
        text.setColor(TEXT_COLOUR).setDropShadow(false);
        button.setTexture(new GuiTextureGroup(GuiTextures.BUTTON, text),
                new GuiTextureGroup(GuiTextures.BACKGROUND, text));
        page.setSelfPosition(PAD, TAB_HEIGHT + PAD);
        page.setSize(pageWidth(), pageHeight());
        tabs.addTab(button, page);
        return this;
    }

    public WidgetGroup build() {
        return root;
    }

    public int totalHeight() {
        return height + TAB_HEIGHT;
    }

    /** A dark label, as GTNH draws text on the grey. */
    public static LabelWidget label(int x, int y, Component text) {
        return new LabelWidget(x, y, text).setTextColor(TEXT_COLOUR).setDropShadow(false);
    }

    /** A label whose text the server supplies; LDLib syncs it when it changes. */
    public static LabelWidget label(int x, int y, Supplier<String> text) {
        return new LabelWidget(x, y, text).setTextColor(TEXT_COLOUR).setDropShadow(false);
    }

    /** A GTCEu button with a text; the action runs on both sides (check {@code ClickData.isRemote}). */
    public static ButtonWidget button(int x, int y, int w, int h, Component text, Consumer<ClickData> action) {
        var label = new TextTexture(text.getString()).setWidth(w - 4).setType(TextTexture.TextType.ROLL);
        label.setColor(0xFFFFFFFF);
        return new ButtonWidget(x, y, w, h, new GuiTextureGroup(GuiTextures.BUTTON, label), action);
    }

    /** An on/off button: label on the left, a lamp-like switch on the right. */
    public static WidgetGroup toggle(int x, int y, int w, Component text, BooleanSupplier value, Runnable flip) {
        var group = new WidgetGroup(x, y, w, 14);
        group.addWidget(label(0, 3, text));
        group.addWidget(new ImageWidget(w - 28, 0, 28, 14,
                () -> new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(value.getAsBoolean() ? "§aON" : "§7OFF"))));
        group.addWidget(new ButtonWidget(w - 28, 0, 28, 14, IGuiTexture.EMPTY, cd -> flip.run()));
        return group;
    }

    /** A number with -/+ buttons and a text field, clamped to [min, max]. */
    public static WidgetGroup number(int x, int y, int w, Component text, int min, int max, IntSupplier value,
                                     IntConsumer set) {
        var group = new WidgetGroup(x, y, w, 30);
        group.addWidget(label(0, 0, text));
        group.addWidget(button(0, 12, 18, 16, Component.literal("-"),
                cd -> set.accept(clamp(value.getAsInt() - (cd.isShiftClick ? 10 : 1), min, max))));
        var field = new TextFieldWidget(20, 12, w - 40, 16, () -> Integer.toString(value.getAsInt()), s -> {
            try {
                set.accept(clamp(Integer.parseInt(s), min, max));
            } catch (NumberFormatException ignored) {}
        });
        field.setNumbersOnly(min, max);
        field.setBackground(GuiTextures.DISPLAY);
        group.addWidget(field);
        group.addWidget(button(w - 18, 12, 18, 16, Component.literal("+"),
                cd -> set.accept(clamp(value.getAsInt() + (cd.isShiftClick ? 10 : 1), min, max))));
        return group;
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
