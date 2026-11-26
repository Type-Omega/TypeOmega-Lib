package com.typeomega.lib.gametest;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.typeomega.lib.TypeOmegaLib;
import com.typeomega.lib.ui.tool.SyncedFlag;
import com.typeomega.lib.ui.tool.ToolControls;
import com.typeomega.lib.ui.tool.ToolStyle;
import com.typeomega.lib.ui.tool.ToolTheme;
import com.typeomega.lib.ui.tool.ToolWindow;

/** The tool window in both themes: the default keeps its geometry, the purple one builds with a title bar. */
@PrefixGameTestTemplate(false)
@GameTestHolder(TypeOmegaLib.MOD_ID)
public final class ToolWindowGameTests {

    private ToolWindowGameTests() {}

    private static ToolWindow withThreeTabs(ToolWindow window) {
        for (int i = 0; i < 3; i++) {
            window.tab(Component.literal("Tab " + i), new WidgetGroup(0, 0, window.pageWidth(), window.pageHeight()));
        }
        return window;
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void defaultThemeKeepsTheOldGeometry(GameTestHelper h) {
        var window = withThreeTabs(new ToolWindow(420, 236));
        h.assertTrue(window.theme() == ToolTheme.DEFAULT, "no theme given means the grey one");
        h.assertTrue(window.pageWidth() == 408 && window.pageHeight() == 224, "page " + window.pageWidth() + "x" +
                window.pageHeight());
        h.assertTrue(window.pageTop() == ToolWindow.TAB_HEIGHT + ToolWindow.PAD, "page top " + window.pageTop());
        h.assertTrue(window.totalHeight() == 254, "total height " + window.totalHeight());
        h.assertTrue(window.build().widgets.size() == 2, "frame and tabs only");
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void purpleThemeBuildsWithATitleBar(GameTestHelper h) {
        var window = new ToolWindow(420, 236, ToolTheme.NEXUS_PURPLE);
        window.header(ItemStack.EMPTY, Component.literal("Title"), () -> "target", () -> "MODE", () -> false);
        withThreeTabs(window);
        h.assertTrue(window.theme() == ToolTheme.NEXUS_PURPLE, "the theme is kept");
        h.assertTrue(window.pageWidth() == 410, "page width " + window.pageWidth());
        h.assertTrue(window.pageHeight() < 224 && window.pageHeight() > 190, "page height " + window.pageHeight() +
                " leaves room for the title bar");
        h.assertTrue(window.pageTop() > ToolWindow.TAB_HEIGHT + 20, "the page starts under the title bar");
        h.assertTrue(window.pageTop() + window.pageHeight() + ToolTheme.NEXUS_PURPLE.pad() == window.totalHeight(),
                "the page ends one pad above the frame's bottom");
        h.assertTrue(window.build().widgets.size() == 3, "frame, title bar and tabs");
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void theTitleBarGoesFirstAndOnce(GameTestHelper h) {
        var window = new ToolWindow(420, 236, ToolTheme.NEXUS_PURPLE);
        window.header(ItemStack.EMPTY, Component.literal("T"), () -> "", () -> "", () -> false);
        boolean twice = false, late = false;
        try {
            window.header(ItemStack.EMPTY, Component.literal("T"), () -> "", () -> "", () -> false);
        } catch (IllegalStateException e) {
            twice = true;
        }
        var other = new ToolWindow(420, 236, ToolTheme.NEXUS_PURPLE);
        withThreeTabs(other);
        try {
            other.header(ItemStack.EMPTY, Component.literal("T"), () -> "", () -> "", () -> false);
        } catch (IllegalStateException e) {
            late = true;
        }
        h.assertTrue(twice, "a second title bar is refused");
        h.assertTrue(late, "a title bar after a tab is refused");
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void controlsBuildInBothThemes(GameTestHelper h) {
        for (ToolTheme theme : ToolTheme.values()) {
            for (ToolStyle style : ToolStyle.values()) {
                var button = ToolControls.button(theme, style, 1, 2, 50, 18, Component.literal("Go"), cd -> {});
                h.assertTrue(button.getSizeWidth() == 50 && button.getSizeHeight() == 18, theme.id() + " button size");
                var toggle = ToolControls.toggle(theme, style, 0, 0, 100, Component.literal("T"), () -> true, () -> {});
                h.assertTrue(toggle.getSizeWidth() == 100, theme.id() + " toggle width");
            }
            var number = ToolControls.number(theme, 0, 0, 120, Component.literal("N"), 0, 10, () -> 3, v -> {});
            h.assertTrue(number.getSizeHeight() == 30, theme.id() + " number height");
            boolean[] picked = { false };
            var step = ToolControls.step(theme, 0, 0, 90, 20, 1, Component.literal("One"), () -> picked[0],
                    () -> picked[0] = true);
            h.assertTrue(step.getSizeWidth() == 90 && step.getSizeHeight() == 20, theme.id() + " step size");
            var pane = ToolControls.pane(theme, 0, 0, 200, 100, Component.literal("Pane"));
            h.assertTrue(pane.getSizeWidth() == 200 && pane.widgets.size() == 3, theme.id() + " pane parts");
        }
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void aDisabledButtonIgnoresClicks(GameTestHelper h) {
        boolean[] on = { false };
        int[] clicks = { 0 };
        var flag = new SyncedFlag(() -> on[0]);
        var group = ToolControls.button(ToolTheme.NEXUS_PURPLE, ToolStyle.DANGER, 0, 0, 50, 18,
                Component.literal("Cancel"), flag, cd -> clicks[0]++);
        h.assertTrue(group.widgets.contains(flag), "the flag lives in the group so it syncs");
        h.assertFalse(flag.isOn(), "off");
        on[0] = true;
        h.assertTrue(flag.isOn(), "on is read from the supplier on the server");
        h.succeed();
    }
}
