package com.typeomega.lib.ui;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.editor.IEditableUI;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;

import java.util.function.BiConsumer;

/**
 * A GTCEu recipe UI drawn on an illustrated panel (GTIA-D-434, U4: {@code setRecipeUI} for recipe types whose screen
 * is built in code). The slots and tanks keep the GTCEu ids ({@code item_in_0}, {@code fluid_out_1}...), so GTCEu
 * fills them in EMI, JEI and in the machine GUI exactly as on its own template. The text under the picture is the
 * panel's: Total, Usage with the tier colour, Time and the lines the recipe type adds.
 *
 * <pre>{@code
 * MY_TYPE.setRecipeUI(new IllustratedRecipeUI(MY_TYPE, LAYOUT, 2, (recipe, lines) -> lines.line(START, ...)));
 * }</pre>
 */
public class IllustratedRecipeUI extends GTRecipeTypeUI {

    private final PanelLayout layout;
    private final int extraLines;
    private final BiConsumer<GTRecipe, InfoLines> extra;

    /**
     * @param extraLines how many lines {@code extra} adds at most, so EMI reserves the height
     * @param extra      adds the lines of this recipe type after Total, Usage and Time
     */
    public IllustratedRecipeUI(GTRecipeType type, PanelLayout layout, int extraLines,
                               BiConsumer<GTRecipe, InfoLines> extra) {
        super(type);
        this.layout = layout;
        this.extraLines = extraLines;
        this.extra = extra;
        setUiBuilder(this::appendLines);
    }

    public PanelLayout layout() {
        return layout;
    }

    public int lineCount() {
        return 3 + extraLines;
    }

    @Override
    public IEditableUI<WidgetGroup, RecipeHolder> createEditableUITemplate(boolean isSteam, boolean isHighPressure) {
        // EN: Only the binder of the GTCEu template is reused: it fills the slots by id and wires the progress bar.
        // PT: Do template do GTCEu só se reaproveita o binder: ele preenche os slots pelo id e liga a barra de
        // progresso.
        // ZH: 只复用 GTCEu 模板的绑定器：它按 id 填充槽位并连接进度条。
        var gtceu = super.createEditableUITemplate(isSteam, isHighPressure);
        return new IEditableUI.Normal<>(this::createPanel, gtceu::setupUI);
    }

    /** The empty panel with GTCEu slot widgets; GTCEu binds the recipe into them. */
    public IllustratedPanel createPanel() {
        var panel = new IllustratedPanel(layout, lineCount());
        panel.slots(slot -> switch (slot.kind()) {
            case ITEM -> itemSlot(slot);
            case FLUID -> tank(slot);
        });
        panel.atMark("progress", mark -> {
            var progress = new ProgressWidget(ProgressWidget.JEIProgress, 0, 0, mark.width(), mark.height(),
                    getProgressBarTexture());
            progress.setId("progress");
            return progress;
        });
        return panel;
    }

    private static IO io(PanelLayout.Slot slot) {
        return slot.role() == PanelLayout.Role.OUTPUT ? IO.OUT : IO.IN;
    }

    private static Widget itemSlot(PanelLayout.Slot slot) {
        var widget = new SlotWidget();
        widget.initTemplate();
        widget.setBackground(GuiTextures.SLOT);
        widget.setId(ItemRecipeCapability.CAP.slotName(io(slot), slot.index()));
        return widget;
    }

    private static Widget tank(PanelLayout.Slot slot) {
        var widget = new TankWidget();
        widget.initTemplate();
        widget.setFillDirection(ProgressTexture.FillDirection.ALWAYS_FULL);
        widget.setBackground(GuiTextures.FLUID_SLOT);
        widget.setId(FluidRecipeCapability.CAP.slotName(io(slot), slot.index()));
        return widget;
    }

    @Override
    public Size getJEISize() {
        return new Size(layout.panelWidth(), layout.panelHeight(lineCount()));
    }

    @Override
    public int getOriginalWidth() {
        return layout.panelWidth();
    }

    /** The lines of one recipe: Total, Usage or Output with the tier colour, Time, then the type's own lines. */
    public InfoLines lines(GTRecipe recipe) {
        return standardLines(recipe, extra);
    }

    /** Total, Usage or Output and Time of a recipe, then {@code extra}; shared with {@link #rtuiLines}. */
    public static InfoLines standardLines(GTRecipe recipe, BiConsumer<GTRecipe, InfoLines> extra) {
        var lines = InfoLines.create();
        var eu = RecipeHelper.getRealEUtWithIO(recipe);
        if (eu.voltage() > 0) {
            long eut = eu.voltage() * eu.amperage();
            lines.total(eut * recipe.duration).energy(eut, !eu.isInput());
        }
        lines.time(recipe.duration);
        extra.accept(recipe, lines);
        return lines;
    }

    /**
     * For a recipe type whose fixed screen comes from a {@code .rtui} file (GTIA-D-434, U4): pass the result to
     * {@code setUiBuilder}. It writes the GTNH-style lines at {@code top} (the picture and the slots are in the
     * {@code .rtui}) and hides the default GTCEu text, as {@link IllustratedRecipeUI} does.
     */
    public static BiConsumer<GTRecipe, WidgetGroup> rtuiLines(int left, int top,
                                                              BiConsumer<GTRecipe, InfoLines> extra) {
        return (recipe, group) -> {
            var lines = new InfoLinesWidget(standardLines(recipe, extra).build());
            lines.setSelfPosition(left, top);
            lines.setSize(Math.max(10, group.getSizeWidth() - left), lines.getSizeHeight());
            group.addWidget(lines);
            group.addWidget(new DefaultTextHider());
        };
    }

    private void appendLines(GTRecipe recipe, WidgetGroup recipeWidget) {
        var panel = recipeWidget.getWidgetsByType(IllustratedPanel.class);
        var lines = new InfoLinesWidget(lines(recipe).build());
        if (panel.isEmpty()) recipeWidget.addWidget(lines);
        else panel.get(0).lines(lines);
        recipeWidget.addWidget(new DefaultTextHider());
    }

    /**
     * Hides the text GTCEu's recipe widget adds under every template (duration, total, EU/t and the clickable tier),
     * since the panel draws its own GTNH-style lines.
     */
    static final class DefaultTextHider extends Widget {

        DefaultTextHider() {
            super(0, 0, 0, 0);
            setClientSideWidget();
        }

        // EN: GTCEu 7.5.3 adds those labels after the UI builder runs, so they are hidden when the screen initialises.
        // PT: O GTCEu 7.5.3 adiciona esses textos depois do UI builder, então eles são escondidos quando a tela
        // inicializa.
        // ZH: GTCEu 7.5.3 在 UI 构建器之后才添加这些文字，因此在界面初始化时将其隐藏。
        @Override
        public void initWidget() {
            super.initWidget();
            if (parent == null) return;
            for (Widget widget : parent.widgets) {
                if (widget instanceof LabelWidget || widget.getClass() == ButtonWidget.class) {
                    widget.setVisible(false);
                    widget.setActive(false);
                }
            }
        }
    }
}
