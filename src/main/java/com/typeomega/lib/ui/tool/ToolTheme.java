package com.typeomega.lib.ui.tool;

import java.util.List;

/**
 * The look of a {@link ToolWindow}: a palette and a few metrics, as plain ints so it can be unit tested without a
 * client. {@link #DEFAULT} is the GTNH grey of the GTCEu textures (the colours below only matter for the text);
 * {@link #NEXUS_PURPLE} is the flat purple and black look drawn entirely in code ({@link PixelFrame}, no 9-slice
 * PNG), in the way of GTCEu Terminals (D-447).
 * <p>
 * EN: a theme is data only; {@link ToolTextures} turns it into LDLib textures and {@link ToolControls} into widgets.
 * PT: um tema é só dado; {@link ToolTextures} o transforma em texturas do LDLib e {@link ToolControls} em widgets.
 * ZH: 主题只是数据；{@link ToolTextures} 把它变成 LDLib 纹理，{@link ToolControls} 变成控件。
 *
 * @param flat       true when the frames are drawn in code; false keeps the GTCEu bevelled textures
 * @param textShadow whether labels get a drop shadow (readable on dark, wrong on the grey)
 * @param pad        space between the frame and a page
 * @param tabWidth   width of a tab button
 */
public record ToolTheme(String id, boolean flat, boolean textShadow, int pad, int tabWidth,
                        int text, int textDim, int textMuted, int textOnDisplay, int title,
                        int window, int panel, int card, int slot,
                        int outline, int line, int border, int accent, int accentBright,
                        int ok, int warn, int err, int pink, int me,
                        ButtonColours normal, ButtonColours primary, ButtonColours danger, ButtonColours success,
                        ButtonColours disabled, ButtonColours tab, ButtonColours tabActive) {

    /** The colours of one button style, per state. */
    public record ButtonColours(int fill, int hover, int pressed, int border, int borderHover, int text) {}

    public static final ToolTheme DEFAULT = new ToolTheme("default", false, false, 6, 72,
            0xFF2B2B2B, 0xFF555555, 0xFF777777, 0xFFFFFFFF, 0xFF2B2B2B,
            0xFFC6C6C6, 0xFFA0A0A0, 0xFF8B8B8B, 0xFF8B8B8B,
            0xFF373737, 0xFF555555, 0xFF555555, 0xFF3F7FD0, 0xFF3F7FD0,
            0xFF55FF55, 0xFFFFAA00, 0xFFFF5555, 0xFFFF55FF, 0xFF55FFFF,
            plain(0xFFFFFFFF), plain(0xFFFFFFFF), plain(0xFFFFFFFF), plain(0xFFFFFFFF), plain(0xFF777777),
            plain(0xFF2B2B2B), plain(0xFF2B2B2B));

    public static final ToolTheme NEXUS_PURPLE = new ToolTheme("nexus_purple", true, true, 5, 110,
            0xFFF5F3FF, 0xFFA79BBE, 0xFF6F638A, 0xFFF5F3FF, 0xFFC084FC,
            0xFF120E1A, 0xFF1A1426, 0xFF221A33, 0xFF100C18,
            0xFF07050A, 0xFF362852, 0xFF7B4FBF, 0xFFA855F7, 0xFFC084FC,
            0xFF22C55E, 0xFFF59E0B, 0xFFEF4444, 0xFFF472B6, 0xFF38BDF8,
            new ButtonColours(0xFF271E3A, 0xFF3A2B59, 0xFF1C142B, 0xFF7B4FBF, 0xFFC084FC, 0xFFF5F3FF),
            new ButtonColours(0xFF581C87, 0xFF7E22CE, 0xFF3B0F63, 0xFFC084FC, 0xFFE9D5FF, 0xFFFFFFFF),
            new ButtonColours(0xFF450A0A, 0xFF7F1D1D, 0xFF2D0606, 0xFFDC2626, 0xFFF87171, 0xFFFECACA),
            new ButtonColours(0xFF14532D, 0xFF166534, 0xFF0D3B20, 0xFF22C55E, 0xFF86EFAC, 0xFFF0FDF4),
            new ButtonColours(0xFF181520, 0xFF181520, 0xFF181520, 0xFF2A2438, 0xFF2A2438, 0xFF8A7FA5),
            new ButtonColours(0xFF1A1426, 0xFF271E3A, 0xFF271E3A, 0xFF362852, 0xFF7B4FBF, 0xFFA79BBE),
            new ButtonColours(0xFF120E1A, 0xFF120E1A, 0xFF120E1A, 0xFF7B4FBF, 0xFF7B4FBF, 0xFFC084FC));

    private static final List<ToolTheme> ALL = List.of(DEFAULT, NEXUS_PURPLE);

    public static List<ToolTheme> values() {
        return ALL;
    }

    /** The theme with this id, or the default one when none matches. */
    public static ToolTheme byId(String id) {
        for (ToolTheme theme : ALL) if (theme.id.equals(id)) return theme;
        return DEFAULT;
    }

    /** The colours of a button style. */
    public ButtonColours colours(ToolStyle style) {
        return switch (style) {
            case NORMAL -> normal;
            case PRIMARY -> primary;
            case DANGER -> danger;
            case SUCCESS -> success;
        };
    }

    private static ButtonColours plain(int text) {
        return new ButtonColours(0, 0, 0, 0, 0, text);
    }
}
