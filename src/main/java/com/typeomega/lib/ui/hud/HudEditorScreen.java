package com.typeomega.lib.ui.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shared HUD editor opened by the TypeOmega Lib keybind or by a machine's HUD button. Draws every
 * enabled {@link MoveableHud} over the live game world, outlines each one, lets the player toggle
 * them and saves dragged positions back to {@link HudRegistry}.
 */
@OnlyIn(Dist.CLIENT)
public class HudEditorScreen extends Screen {

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STEP_X = 154;
    private static final int BUTTON_STEP_Y = 22;
    private static final int DONE_RESERVE = 72;
    private static final int OUTLINE_COLOR = 0xFF7FDBFF;

    private int stripTop = 26;
    private MoveableHud activeHud;
    private int dragStartX;
    private int dragStartY;
    private int pendingX;
    private int pendingY;

    public HudEditorScreen() {
        super(HudTexts.EDITOR_TITLE.component());
    }

    @Override
    protected void init() {
        super.init();
        int x = 6;
        int y = 6;
        for (MoveableHud hud : HudRegistry.all()) {
            // EN: Wrap to another row before colliding with the top-right Done button.
            // PT: Quebra para outra linha antes de encostar no botão Concluir no canto direito.
            // ZH: 在碰到右上角的完成按钮之前自动换行。
            if (x > 6 && x + BUTTON_WIDTH > width - DONE_RESERVE) {
                x = 6;
                y += BUTTON_STEP_Y;
            }
            addRenderableWidget(Button.builder(toggleLabel(hud), button -> {
                hud.setEnabled(!hud.isEnabled());
                button.setMessage(toggleLabel(hud));
            }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            x += BUTTON_STEP_X;
        }
        stripTop = y + BUTTON_HEIGHT;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width - 66, 6, 60, BUTTON_HEIGHT).build());
    }

    private static Component toggleLabel(MoveableHud hud) {
        return Component.empty().append(hud.displayName()).append(": ")
                .append((hud.isEnabled() ? HudTexts.EDITOR_ON : HudTexts.EDITOR_OFF).component());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // EN: Keep the world visible so HUDs are placed against the real scene (only a strip behind labels).
        // PT: Mantém o mundo visível para posicionar os HUDs na cena real (só uma faixa atrás dos textos).
        // ZH: 保持游戏画面可见以便对照真实场景摆放 HUD（仅在文字后方绘制半透明条）。
        graphics.fill(0, stripTop, width, stripTop + 30, 0x80000000);

        for (MoveableHud hud : HudRegistry.all()) {
            if (!hud.isEnabled()) continue;
            Rect2i bounds = draggedBounds(hud);
            hud.render(graphics, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            MoveableHud.drawOutline(graphics, bounds, OUTLINE_COLOR);
        }

        graphics.drawCenteredString(font, title, width / 2, stripTop + 4, 0xFFFFFFFF);
        graphics.drawCenteredString(font, HudTexts.EDITOR_HINT.component(), width / 2, stripTop + 18, 0xFFAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Rect2i draggedBounds(MoveableHud hud) {
        Rect2i base = HudRegistry.bounds(hud, width, height);
        if (hud != activeHud) return base;
        int maxX = Math.max(0, width - base.getWidth());
        int maxY = Math.max(0, height - base.getHeight());
        return new Rect2i(
                Mth.clamp(base.getX() + pendingX, 0, maxX),
                Mth.clamp(base.getY() + pendingY, 0, maxY),
                base.getWidth(),
                base.getHeight());
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics) {
        // Intentionally empty: the editor must not dim the world behind the HUD.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) return false;
        List<MoveableHud> huds = HudRegistry.all();
        for (int i = huds.size() - 1; i >= 0; i--) {
            MoveableHud hud = huds.get(i);
            if (hud.isEnabled() && HudRegistry.bounds(hud, width, height).contains((int) mouseX, (int) mouseY)) {
                activeHud = hud;
                dragStartX = (int) mouseX;
                dragStartY = (int) mouseY;
                pendingX = 0;
                pendingY = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (activeHud != null && button == 0) {
            pendingX = (int) mouseX - dragStartX;
            pendingY = (int) mouseY - dragStartY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeHud != null && button == 0) {
            if (pendingX != 0 || pendingY != 0) {
                Rect2i base = HudRegistry.bounds(activeHud, width, height);
                HudRegistry.setTopLeftPosition(activeHud, base.getX() + pendingX, base.getY() + pendingY, width,
                        height);
            }
            activeHud = null;
            pendingX = 0;
            pendingY = 0;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        activeHud = null;
        pendingX = 0;
        pendingY = 0;
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
