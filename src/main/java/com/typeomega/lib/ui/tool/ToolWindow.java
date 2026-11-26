package com.typeomega.lib.ui.tool;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TabButton;
import com.lowdragmc.lowdraglib.gui.widget.TabContainer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * A tool window: a frame, a row of tabs on top and one page per tab. The look comes from a {@link ToolTheme}: the
 * default is the GTNH look (D-435, the GTCEu bevelled grey); {@link ToolTheme#NEXUS_PURPLE} (D-447) is the flat
 * purple and black one drawn in code, which can also carry a title bar ({@link #header}). Used by held-item tools
 * such as the Nexus Terminal; the page widgets come from the mod, the frame and the small controls come from here
 * so every Type Omega tool looks the same unless it picks another theme.
 */
public final class ToolWindow {

    public static final int TAB_WIDTH = 72;
    public static final int TAB_HEIGHT = 18;
    public static final int TEXT_COLOUR = 0xFF2B2B2B;
    public static final int PAD = 6;
    /** Gap between the title bar and the page of a flat theme. */
    private static final int HEADER_GAP = 3;

    private final int width, height;
    private final ToolTheme theme;
    private final WidgetGroup root;
    private final TabContainer tabs;
    private int tabCount, headerHeight;

    public ToolWindow(int width, int height) {
        this(width, height, ToolTheme.DEFAULT);
    }

    public ToolWindow(int width, int height, ToolTheme theme) {
        this.width = width;
        this.height = height;
        this.theme = theme;
        this.root = new WidgetGroup(0, 0, width, height + TAB_HEIGHT);
        this.tabs = new TabContainer(0, 0, width, height + TAB_HEIGHT);
        root.addWidget(new ImageWidget(0, TAB_HEIGHT - 2, width, height + 2, ToolTextures.window(theme)));
        root.addWidget(tabs);
    }

    public ToolTheme theme() {
        return theme;
    }

    /** Top of a page, below the tabs and the title bar when there is one. */
    public int pageTop() {
        return headerHeight == 0 ? TAB_HEIGHT + theme.pad() : TAB_HEIGHT + headerHeight + HEADER_GAP;
    }

    /** Size of the area a page gets, inside the frame (and under the title bar, when there is one). */
    public int pageWidth() {
        return width - 2 * theme.pad();
    }

    public int pageHeight() {
        return totalHeight() - pageTop() - theme.pad();
    }

    /**
     * Adds a title bar under the tabs: the item as an icon, a title, a subtitle and a badge (both filled by the
     * server) and an alert flag that turns the bar and the badge red. Call it before the first tab.
     */
    public ToolWindow header(ItemStack icon, Component title, Supplier<String> subtitle, Supplier<String> badge,
                             BooleanSupplier alert) {
        if (tabCount > 0 || headerHeight > 0) throw new IllegalStateException("header goes first, once");
        headerHeight = ToolHeader.HEIGHT;
        int inset = theme.flat() ? 2 : theme.pad();
        var bar = ToolHeader.build(theme, width - 2 * inset, title, subtitle, badge, alert, icon);
        bar.setSelfPosition(inset, TAB_HEIGHT + (theme.flat() ? 0 : theme.pad() - 2));
        root.addWidget(1, bar);
        return this;
    }

    /** Adds a tab with its title; the page is placed inside the frame and shown when the tab is picked. */
    public ToolWindow tab(Component title, WidgetGroup page) {
        int tw = theme.tabWidth();
        int x = tabCount++ * (tw + 2);
        var button = new TabButton(x, 0, tw, TAB_HEIGHT);
        String name = title.getString();
        if (theme.flat()) {
            var idle = ToolControls.text(theme, theme.tab().text(), tw - 6, name);
            var active = ToolControls.text(theme, theme.tabActive().text(), tw - 6, name);
            button.setTexture(new GuiTextureGroup(ToolTextures.tab(theme, false, false), idle),
                    new GuiTextureGroup(ToolTextures.tab(theme, true, false), active));
            button.setHoverTexture(new PixelFrame(0x22FFFFFF, PixelFrame.ALL & ~PixelFrame.BOTTOM,
                    theme.tab().borderHover()));
        } else {
            var text = ToolControls.text(theme, TEXT_COLOUR, tw - 4, name);
            text.setDropShadow(false);
            button.setTexture(new GuiTextureGroup(ToolTextures.tab(theme, false, false), text),
                    new GuiTextureGroup(ToolTextures.tab(theme, true, false), text));
        }
        page.setSelfPosition(theme.pad(), pageTop());
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
        return ToolControls.label(ToolTheme.DEFAULT, x, y, text);
    }

    /** A label whose text the server supplies; LDLib syncs it when it changes. */
    public static LabelWidget label(int x, int y, Supplier<String> text) {
        return ToolControls.label(ToolTheme.DEFAULT, x, y, text);
    }

    /** A GTCEu button with a text; the action runs on both sides (check {@code ClickData.isRemote}). */
    public static ButtonWidget button(int x, int y, int w, int h, Component text, Consumer<ClickData> action) {
        return ToolControls.button(ToolTheme.DEFAULT, ToolStyle.NORMAL, x, y, w, h, text, action);
    }

    /** An on/off button: label on the left, a lamp-like switch on the right. */
    public static WidgetGroup toggle(int x, int y, int w, Component text, BooleanSupplier value, Runnable flip) {
        return ToolControls.toggle(ToolTheme.DEFAULT, ToolStyle.NORMAL, x, y, w, text, value, flip);
    }

    /** A number with -/+ buttons and a text field, clamped to [min, max]. */
    public static WidgetGroup number(int x, int y, int w, Component text, int min, int max, IntSupplier value,
                                     IntConsumer set) {
        return ToolControls.number(ToolTheme.DEFAULT, x, y, w, text, min, max, value, set);
    }

    static int clamp(int value, int min, int max) {
        return ToolControls.clamp(value, min, max);
    }
}
