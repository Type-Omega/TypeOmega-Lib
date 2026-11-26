package com.typeomega.lib.ui.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolThemeTest {

    private static double luminance(int argb) {
        double[] c = { (argb >> 16 & 0xFF) / 255.0, (argb >> 8 & 0xFF) / 255.0, (argb & 0xFF) / 255.0 };
        for (int i = 0; i < 3; i++) c[i] = c[i] <= 0.03928 ? c[i] / 12.92 : Math.pow((c[i] + 0.055) / 1.055, 2.4);
        return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
    }

    private static double contrast(int a, int b) {
        double la = luminance(a), lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    @Test
    void defaultThemeKeepsTheGreyLook() {
        ToolTheme t = ToolTheme.DEFAULT;
        assertEquals("default", t.id());
        assertFalse(t.flat(), "the default look keeps the GTCEu bevelled textures");
        assertFalse(t.textShadow());
        assertEquals(ToolWindow.TEXT_COLOUR, t.text(), "dark text on the grey, as before");
        assertEquals(ToolWindow.PAD, t.pad());
        assertEquals(ToolWindow.TAB_WIDTH, t.tabWidth());
    }

    @Test
    void nexusPurpleIsFlatAndDark() {
        ToolTheme t = ToolTheme.NEXUS_PURPLE;
        assertTrue(t.flat());
        assertTrue(t.textShadow());
        assertTrue(luminance(t.window()) < 0.02, "window is near black");
        assertTrue(luminance(t.text()) > 0.8, "text is near white");
    }

    @Test
    void themesAreFoundById() {
        assertSame(ToolTheme.NEXUS_PURPLE, ToolTheme.byId("nexus_purple"));
        assertSame(ToolTheme.DEFAULT, ToolTheme.byId("default"));
        assertSame(ToolTheme.DEFAULT, ToolTheme.byId("nope"), "an unknown id falls back to the default");
        assertSame(ToolTheme.DEFAULT, ToolTheme.byId(null));
    }

    @Test
    void idsAreUniqueAndOnlyTheNewThemeIsFlat() {
        var ids = ToolTheme.values().stream().map(ToolTheme::id).distinct().count();
        assertEquals(ToolTheme.values().size(), ids);
        assertEquals(1, ToolTheme.values().stream().filter(ToolTheme::flat).count());
    }

    @Test
    void everyStyleHasItsOwnColours() {
        ToolTheme t = ToolTheme.NEXUS_PURPLE;
        for (ToolStyle style : ToolStyle.values()) assertNotNull(t.colours(style));
        assertNotEquals(t.colours(ToolStyle.NORMAL).fill(), t.colours(ToolStyle.DANGER).fill());
        assertNotEquals(t.colours(ToolStyle.NORMAL).fill(), t.colours(ToolStyle.NORMAL).hover(), "hover shows");
        assertNotEquals(t.colours(ToolStyle.NORMAL).fill(), t.colours(ToolStyle.NORMAL).pressed(), "press shows");
        assertNotEquals(t.disabled().border(), t.normal().border(), "disabled looks different");
    }

    @Test
    void textIsReadableOnItsBackgroundsInTheFlatTheme() {
        ToolTheme t = ToolTheme.NEXUS_PURPLE;
        assertTrue(contrast(t.text(), t.window()) >= 7, "text on the window");
        assertTrue(contrast(t.text(), t.card()) >= 7, "text on a card");
        assertTrue(contrast(t.textDim(), t.panel()) >= 4.5, "secondary text on a panel");
        assertTrue(contrast(t.textOnDisplay(), t.slot()) >= 7, "text on a display");
        assertTrue(contrast(t.title(), t.panel()) >= 4.5, "title on the title bar");
        for (ToolStyle style : ToolStyle.values()) {
            var c = t.colours(style);
            assertTrue(contrast(c.text(), c.fill()) >= 4.5, style + " label on its fill");
            assertTrue(contrast(c.text(), c.hover()) >= 4.5, style + " label on hover");
        }
        assertTrue(contrast(t.disabled().text(), t.disabled().fill()) >= 3, "disabled is dim but legible");
        assertTrue(contrast(t.tab().text(), t.tab().fill()) >= 4.5, "idle tab");
        assertTrue(contrast(t.tabActive().text(), t.tabActive().fill()) >= 4.5, "active tab");
    }
}
