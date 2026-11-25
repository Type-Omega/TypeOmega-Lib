package com.typeomega.lib.ui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * A side tab of a GTCEu machine GUI that shows an illustrated panel, so the machine shows the same picture as its EMI
 * page (D-435, U6). Attach it from {@code attachSideTabs}.
 */
public record IllustratedTab(IGuiTexture icon, Component title, Supplier<Widget> page) implements IFancyUIProvider {

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return page.get();
    }

    @Override
    public IGuiTexture getTabIcon() {
        return icon;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }
}
