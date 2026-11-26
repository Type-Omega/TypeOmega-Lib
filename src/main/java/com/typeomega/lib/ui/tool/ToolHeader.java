package com.typeomega.lib.ui.tool;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The title bar of a {@link ToolWindow} under its tabs: an optional item icon, a title, a subtitle (the target,
 * filled by the server) and a mode badge on the right that turns red while {@code alert} is on.
 */
final class ToolHeader {

    static final int HEIGHT = 22, BADGE_WIDTH = 104;

    private ToolHeader() {}

    static WidgetGroup build(ToolTheme t, int width, Component title, Supplier<String> subtitle,
                             Supplier<String> badge, BooleanSupplier alert, ItemStack icon) {
        var group = new WidgetGroup(0, 0, width, HEIGHT);
        IGuiTexture calm = ToolTextures.titleBar(t, false), red = ToolTextures.titleBar(t, true);
        group.addWidget(new ImageWidget(0, 0, width, HEIGHT, () -> alert.getAsBoolean() ? red : calm));
        int textX = 5;
        if (!icon.isEmpty()) {
            group.addWidget(new ImageWidget(3, 3, 16, 16, new ItemStackTexture(icon)));
            textX = 23;
        }
        group.addWidget(new LabelWidget(textX, 4, title).setTextColor(t.title()).setDropShadow(t.textShadow()));
        group.addWidget(ToolControls.dimLabel(t, textX, 13, subtitle));
        IGuiTexture badgeCalm = ToolTextures.badge(t, false), badgeRed = ToolTextures.badge(t, true);
        int bx = width - BADGE_WIDTH - 4;
        group.addWidget(new ImageWidget(bx, 4, BADGE_WIDTH, 14, () -> alert.getAsBoolean() ? badgeRed : badgeCalm));
        group.addWidget(new LabelWidget(bx + 5, 7, badge).setTextColor(t.text()).setDropShadow(t.textShadow()));
        return group;
    }
}
