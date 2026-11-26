package com.typeomega.lib.ui.tool;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * The small controls of a {@link ToolWindow} in a given {@link ToolTheme}: labels, buttons, toggles and number
 * fields. With {@link ToolTheme#DEFAULT} they are the GTNH grey ones every Type Omega tool has always had; a flat
 * theme draws them with {@link PixelFrame}.
 */
public final class ToolControls {

    public static final int TOGGLE_HEIGHT = 16;
    /** The label colour on a GTCEu grey button. */
    private static final int GREY_BUTTON_TEXT = 0xFFFFFFFF;

    private ToolControls() {}

    public static LabelWidget label(ToolTheme t, int x, int y, Component text) {
        return new LabelWidget(x, y, text).setTextColor(t.text()).setDropShadow(t.textShadow());
    }

    /** A label whose text the server supplies; LDLib syncs it when it changes. */
    public static LabelWidget label(ToolTheme t, int x, int y, Supplier<String> text) {
        return new LabelWidget(x, y, text).setTextColor(t.text()).setDropShadow(t.textShadow());
    }

    /** A label for text that sits on a {@link ToolTextures#display} area. */
    public static LabelWidget displayLabel(ToolTheme t, int x, int y, Supplier<String> text) {
        return new LabelWidget(x, y, text).setTextColor(t.textOnDisplay()).setDropShadow(t.textShadow());
    }

    /** A smaller-contrast label for secondary text. */
    public static LabelWidget dimLabel(ToolTheme t, int x, int y, Supplier<String> text) {
        return new LabelWidget(x, y, text).setTextColor(t.flat() ? t.textDim() : t.text())
                .setDropShadow(t.textShadow());
    }

    static TextTexture text(ToolTheme t, int colour, int width, String text) {
        var texture = new TextTexture(text).setWidth(width).setType(TextTexture.TextType.ROLL);
        texture.setColor(colour);
        if (t.flat()) texture.setDropShadow(t.textShadow());
        return texture;
    }

    /** A button with a text; the action runs on both sides (check {@code ClickData.isRemote}). */
    public static ButtonWidget button(ToolTheme t, ToolStyle style, int x, int y, int w, int h, Component text,
                                      Consumer<ClickData> action) {
        var label = text(t, t.colours(style).text(), w - 4, text.getString());
        var button = new ButtonWidget(x, y, w, h,
                new GuiTextureGroup(ToolTextures.button(t, style, ToolTextures.State.IDLE), label), action);
        if (t.flat()) {
            button.setHoverTexture(new GuiTextureGroup(ToolTextures.button(t, style, ToolTextures.State.HOVER),
                    label));
            button.setClickedTexture(new GuiTextureGroup(ToolTextures.button(t, style, ToolTextures.State.PRESSED),
                    label));
        }
        return button;
    }

    /**
     * A button that looks disabled and ignores clicks while {@code enabled} is off. The flag is added to the group,
     * so it syncs like any other widget.
     */
    public static WidgetGroup button(ToolTheme t, ToolStyle style, int x, int y, int w, int h, Component text,
                                     SyncedFlag enabled, Consumer<ClickData> action) {
        var group = new WidgetGroup(x, y, w, h);
        group.addWidget(enabled);
        group.addWidget(button(t, style, 0, 0, w, h, text, cd -> {
            if (enabled.isOn()) action.accept(cd);
        }));
        IGuiTexture off = new GuiTextureGroup(ToolTextures.buttonDisabled(t),
                text(t, t.disabled().text(), w - 4, text.getString()));
        group.addWidget(new ImageWidget(0, 0, w, h, () -> enabled.isOn() ? IGuiTexture.EMPTY : off));
        return group;
    }

    /** An on/off row: label on the left, a lamp-like switch on the right. */
    public static WidgetGroup toggle(ToolTheme t, ToolStyle style, int x, int y, int w, Component text,
                                     BooleanSupplier value, Runnable flip) {
        if (!t.flat()) {
            var group = new WidgetGroup(x, y, w, 14);
            group.addWidget(label(t, 0, 3, text));
            group.addWidget(new ImageWidget(w - 28, 0, 28, 14,
                    () -> new GuiTextureGroup(GuiTextures.BUTTON,
                            new TextTexture(value.getAsBoolean() ? "§aON" : "§7OFF"))));
            group.addWidget(new ButtonWidget(w - 28, 0, 28, 14, IGuiTexture.EMPTY, cd -> flip.run()));
            return group;
        }
        var group = new WidgetGroup(x, y, w, TOGGLE_HEIGHT);
        IGuiTexture rowOn = ToolTextures.toggleRow(t, true, style), rowOff = ToolTextures.toggleRow(t, false, style);
        group.addWidget(new ImageWidget(0, 0, w, TOGGLE_HEIGHT, () -> value.getAsBoolean() ? rowOn : rowOff));
        group.addWidget(label(t, 5, 4, text));
        var onColours = t.colours(style == ToolStyle.DANGER ? ToolStyle.DANGER : ToolStyle.SUCCESS);
        IGuiTexture pillOn = new GuiTextureGroup(ToolTextures.pill(t, true, style),
                text(t, onColours.text(), 24, "ON")),
                pillOff = new GuiTextureGroup(ToolTextures.pill(t, false, style),
                        text(t, t.textMuted(), 24, "OFF"));
        group.addWidget(
                new ImageWidget(w - 30, 2, 26, TOGGLE_HEIGHT - 4, () -> value.getAsBoolean() ? pillOn : pillOff));
        var click = new ButtonWidget(0, 0, w, TOGGLE_HEIGHT, IGuiTexture.EMPTY, cd -> flip.run());
        click.setHoverTexture(ToolTextures.hoverRing(t, style));
        group.addWidget(click);
        return group;
    }

    /** A number with -/+ buttons and a text field, clamped to [min, max]. */
    public static WidgetGroup number(ToolTheme t, int x, int y, int w, Component text, int min, int max,
                                     IntSupplier value, IntConsumer set) {
        var group = new WidgetGroup(x, y, w, 30);
        int in = t.flat() ? 3 : 0;
        if (t.flat()) group.addWidget(new ImageWidget(0, 0, w, 30, ToolTextures.card(t, false)));
        group.addWidget(label(t, in + (t.flat() ? 1 : 0), t.flat() ? 2 : 0, text));
        group.addWidget(button(t, ToolStyle.NORMAL, in, 12, 18, 16, Component.literal("-"),
                cd -> set.accept(clamp(value.getAsInt() - (cd.isShiftClick ? 10 : 1), min, max))));
        var field = new TextFieldWidget(in + 20, 12, w - 2 * in - 40, 16, () -> Integer.toString(value.getAsInt()),
                s -> {
                    try {
                        set.accept(clamp(Integer.parseInt(s), min, max));
                    } catch (NumberFormatException ignored) {}
                });
        field.setNumbersOnly(min, max);
        field.setBackground(ToolTextures.display(t));
        if (t.flat()) field.setBordered(false).setTextColor(t.text());
        group.addWidget(field);
        group.addWidget(button(t, ToolStyle.NORMAL, w - in - 18, 12, 18, 16, Component.literal("+"),
                cd -> set.accept(clamp(value.getAsInt() + (cd.isShiftClick ? 10 : 1), min, max))));
        return group;
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Where the content of a {@link #pane} starts, and its side and bottom margin. */
    public static final int PANE_TOP = 18, PANE_PAD = 5;

    /**
     * A titled panel: the panel frame, the title in the accent colour and a rule under it. Put the content in a group
     * at {@code (PANE_PAD, PANE_TOP)}; extra controls for the title row (a small button) go at {@code y = 2}.
     */
    public static WidgetGroup pane(ToolTheme t, int x, int y, int w, int h, Component title) {
        var group = new WidgetGroup(x, y, w, h);
        group.addWidget(new ImageWidget(0, 0, w, h, ToolTextures.panel(t)));
        group.addWidget(new LabelWidget(PANE_PAD + 1, 5, title).setTextColor(t.flat() ? t.accentBright() : t.text())
                .setDropShadow(t.textShadow()));
        group.addWidget(new ImageWidget(PANE_PAD, PANE_TOP - 3, w - 2 * PANE_PAD, 1, ToolTextures.rule(t)));
        return group;
    }

    /**
     * A row of a step list: a number box, the step name and a ring when the mouse is on it. {@code active} is read
     * on the client; {@code select} runs on both sides, like a button action.
     */
    public static WidgetGroup step(ToolTheme t, int x, int y, int w, int h, int number, Component text,
                                   BooleanSupplier active, Runnable select) {
        var group = new WidgetGroup(x, y, w, h);
        IGuiTexture on = new GuiTextureGroup(ToolTextures.step(t, true),
                text(t, t.flat() ? t.text() : GREY_BUTTON_TEXT, w - 24, text.getString()));
        IGuiTexture off = new GuiTextureGroup(ToolTextures.step(t, false),
                text(t, t.flat() ? t.textDim() : GREY_BUTTON_TEXT, w - 24, text.getString()));
        group.addWidget(new ImageWidget(0, 0, w, h, () -> active.getAsBoolean() ? on : off));
        if (t.flat()) {
            IGuiTexture numOn = new GuiTextureGroup(ToolTextures.stepNumber(t, true),
                    text(t, t.window(), 14, Integer.toString(number)));
            IGuiTexture numOff = new GuiTextureGroup(ToolTextures.stepNumber(t, false),
                    text(t, t.accentBright(), 14, Integer.toString(number)));
            group.addWidget(new ImageWidget(3, (h - 14) / 2, 14, 14, () -> active.getAsBoolean() ? numOn : numOff));
        }
        var click = new ButtonWidget(0, 0, w, h, IGuiTexture.EMPTY, cd -> select.run());
        click.setHoverTexture(ToolTextures.hoverRing(t, ToolStyle.NORMAL));
        group.addWidget(click);
        return group;
    }
}
