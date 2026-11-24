package com.typeomega.lib.ui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.CycleFluidTransfer;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Read-only slots and tanks for an illustrated panel that is not a GTCEu recipe: an EMI page of its own or a machine
 * tab (GTIA-D-435, U6). Each one tells EMI and JEI its role, so LDLib's {@code ModularEmiRecipe} lists it as an input,
 * output or catalyst and the recipe tree and "uses" lookups find it.
 */
public final class DisplaySlots {

    private DisplaySlots() {}

    public static IngredientIO io(PanelLayout.Role role) {
        return switch (role) {
            case INPUT -> IngredientIO.INPUT;
            case OUTPUT -> IngredientIO.OUTPUT;
            case CATALYST -> IngredientIO.CATALYST;
        };
    }

    /** An item slot cycling through {@code stacks} (an empty list draws an empty slot). */
    public static SlotWidget item(List<ItemStack> stacks, PanelLayout.Role role) {
        var handler = new CycleItemStackHandler(List.of(stacks.isEmpty() ? List.of(ItemStack.EMPTY) : stacks));
        var slot = new SlotWidget(handler, 0, 0, 0, false, false);
        slot.setBackgroundTexture(GuiTextures.SLOT);
        slot.setIngredientIO(io(role));
        return slot;
    }

    /** Same as {@link #item(List, PanelLayout.Role)} with a chance, drawn as the GTCEu chance overlay. */
    public static SlotWidget item(List<ItemStack> stacks, PanelLayout.Role role, float chance) {
        var slot = item(stacks, role);
        slot.setXEIChance(chance);
        return slot;
    }

    /** A tank cycling through {@code fluids}, always drawn full like the GTCEu recipe tanks. */
    public static TankWidget fluid(List<FluidStack> fluids, PanelLayout.Role role) {
        var tank = new TankWidget(new CycleFluidTransfer(List.of(fluids)), 0, 0, 0, PanelLayout.SLOT,
                PanelLayout.SLOT, false, false);
        tank.setBackground(GuiTextures.FLUID_SLOT);
        tank.setFillDirection(ProgressTexture.FillDirection.ALWAYS_FULL);
        tank.setShowAmount(true);
        tank.setDrawHoverTips(true);
        tank.setIngredientIO(io(role));
        return tank;
    }
}
