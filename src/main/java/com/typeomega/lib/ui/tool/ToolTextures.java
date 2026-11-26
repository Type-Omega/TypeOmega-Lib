package com.typeomega.lib.ui.tool;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

/**
 * The textures of a {@link ToolTheme}. The grey theme returns the GTCEu ones; a flat theme returns
 * {@link PixelFrame}s in its colours, so nothing here needs a PNG.
 */
public final class ToolTextures {

    private ToolTextures() {}

    /** The frame of the whole window: a dark outline, a coloured line, then the window colour. */
    public static IGuiTexture window(ToolTheme t) {
        return t.flat() ? PixelFrame.of(t.window(), t.outline(), t.border()) : GuiTextures.BACKGROUND;
    }

    /** A recessed area for lists, summaries and read-outs. */
    public static IGuiTexture display(ToolTheme t) {
        return t.flat() ? PixelFrame.of(t.slot(), t.line()) : GuiTextures.DISPLAY;
    }

    /** A raised block of controls (a step column, a page). */
    public static IGuiTexture panel(ToolTheme t) {
        return t.flat() ? PixelFrame.of(t.panel(), t.line()) : GuiTextures.DISPLAY;
    }

    /** A row or card inside a panel. */
    public static IGuiTexture card(ToolTheme t, boolean highlighted) {
        if (!t.flat()) return GuiTextures.DISPLAY;
        return PixelFrame.of(highlighted ? t.panel() : t.card(), highlighted ? t.accentBright() : t.line());
    }

    public static IGuiTexture slot(ToolTheme t) {
        return t.flat() ? PixelFrame.of(t.slot(), t.line()) : GuiTextures.SLOT;
    }

    /** A slot with the selection ring on it. */
    public static IGuiTexture slotSelected(ToolTheme t) {
        return t.flat() ? PixelFrame.of(t.card(), t.accentBright(), t.accent()) : GuiTextures.SLOT;
    }

    public enum State {
        IDLE,
        HOVER,
        PRESSED
    }

    /** A button of a style in a state. The grey theme has one texture; its hover is the same. */
    public static IGuiTexture button(ToolTheme t, ToolStyle style, State state) {
        if (!t.flat()) return GuiTextures.BUTTON;
        var c = t.colours(style);
        return switch (state) {
            case IDLE -> PixelFrame.of(c.fill(), c.border());
            case HOVER -> PixelFrame.of(c.hover(), c.borderHover());
            case PRESSED -> PixelFrame.of(c.pressed(), c.borderHover());
        };
    }

    public static IGuiTexture buttonDisabled(ToolTheme t) {
        if (!t.flat()) return GuiTextures.BUTTON;
        var c = t.disabled();
        return PixelFrame.of(c.fill(), c.border());
    }

    /**
     * A tab. The active one has no bottom ring and is as tall as the frame's top edge needs, so it opens onto the
     * page below it.
     */
    public static IGuiTexture tab(ToolTheme t, boolean active, boolean hover) {
        if (!t.flat()) return active ? GuiTextures.BACKGROUND : GuiTextures.BUTTON;
        if (active) {
            return new PixelFrame(t.tabActive().fill(), PixelFrame.ALL & ~PixelFrame.BOTTOM, t.outline(),
                    t.tabActive().border());
        }
        var c = t.tab();
        return PixelFrame.of(hover ? c.hover() : c.fill(), hover ? c.borderHover() : c.border());
    }

    /** The title bar under the tabs: a panel with a coloured line along its bottom edge. */
    public static IGuiTexture titleBar(ToolTheme t, boolean alert) {
        if (!t.flat()) return GuiTextures.DISPLAY;
        int line = alert ? t.err() : t.border();
        return new PixelFrame(t.panel(), PixelFrame.BOTTOM | PixelFrame.TOP, t.line(), line);
    }

    /** A small label box (the mode badge in the title bar). */
    public static IGuiTexture badge(ToolTheme t, boolean alert) {
        if (!t.flat()) return GuiTextures.DISPLAY;
        return PixelFrame.of(alert ? t.danger().fill() : t.slot(), alert ? t.err() : t.border());
    }

    /** A warning panel: the red frame of an active dangerous mode. */
    public static IGuiTexture alert(ToolTheme t) {
        if (!t.flat()) return GuiTextures.DISPLAY;
        return PixelFrame.of(t.danger().pressed(), t.err());
    }

    /** The on/off pill of a toggle. */
    public static IGuiTexture pill(ToolTheme t, boolean on, ToolStyle style) {
        if (!t.flat()) return GuiTextures.BUTTON;
        var c = on ? t.colours(style == ToolStyle.DANGER ? ToolStyle.DANGER : ToolStyle.SUCCESS) : t.disabled();
        return PixelFrame.of(c.fill(), c.border());
    }

    /** A toggle row; a danger toggle that is on turns red. */
    public static IGuiTexture toggleRow(ToolTheme t, boolean on, ToolStyle style) {
        if (!t.flat()) return GuiTextures.DISPLAY;
        if (on && style == ToolStyle.DANGER) return PixelFrame.of(t.danger().pressed(), t.err());
        return PixelFrame.of(t.card(), t.line());
    }

    /** A ring with no fill, drawn over a control while the mouse is on it. */
    public static IGuiTexture hoverRing(ToolTheme t, ToolStyle style) {
        return t.flat() ? PixelFrame.of(0, t.colours(style == ToolStyle.DANGER ? style : ToolStyle.NORMAL)
                .borderHover()) : IGuiTexture.EMPTY;
    }

    /** A row of a step list: the active one is filled and ringed in the accent colour. */
    public static IGuiTexture step(ToolTheme t, boolean active) {
        if (!t.flat()) return active ? GuiTextures.BACKGROUND : GuiTextures.BUTTON;
        return active ? PixelFrame.of(t.primary().pressed(), t.accentBright()) : PixelFrame.of(t.card(), t.line());
    }

    /** The small number box at the left of a step. */
    public static IGuiTexture stepNumber(ToolTheme t, boolean active) {
        if (!t.flat()) return IGuiTexture.EMPTY;
        return active ? PixelFrame.of(t.accent(), t.accentBright()) : PixelFrame.of(t.slot(), t.line());
    }

    /** The thin line under a pane's title. */
    public static IGuiTexture rule(ToolTheme t) {
        return t.flat() ? new PixelFrame(t.line(), 0) : IGuiTexture.EMPTY;
    }

    /** A bar of actions along the bottom of a page: a recessed area with the accent line around it. */
    public static IGuiTexture bar(ToolTheme t) {
        return t.flat() ? PixelFrame.of(t.slot(), t.border()) : GuiTextures.DISPLAY;
    }
}
