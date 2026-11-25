package com.typeomega.lib.ui.hud;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * Registers the single HUD editor keybind and renders every enabled {@link MoveableHud} in-game.
 */
@OnlyIn(Dist.CLIENT)
public final class HudClient {

    private static KeyMapping editorKey;

    private HudClient() {}

    /** Called once from the lib constructor on the client only. */
    @SuppressWarnings("removal")
    public static void init() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(HudClient::registerKey);
        modBus.addListener(HudClient::registerOverlay);
        MinecraftForge.EVENT_BUS.addListener(HudClient::onClientTick);
    }

    private static void registerKey(RegisterKeyMappingsEvent event) {
        editorKey = new KeyMapping(
                HudTexts.KEY_EDITOR.key(),
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_H,
                HudTexts.KEY_CATEGORY);
        event.register(editorKey);
    }

    private static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("hud_overlays", HudClient::renderOverlays);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || editorKey == null) return;
        Minecraft mc = Minecraft.getInstance();
        while (editorKey.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new HudEditorScreen());
            }
        }
    }

    private static void renderOverlays(ForgeGui gui, GuiGraphics graphics, float partialTick,
                                       int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui || mc.options.renderDebug) return;
        // EN: HudEditorScreen renders enabled HUDs itself with drag offsets and outlines.
        // PT: O HudEditorScreen já desenha os HUDs ligados com o deslocamento do arraste e o contorno.
        // ZH: HudEditorScreen 会自行绘制已启用的 HUD 及其拖动偏移与边框。
        if (mc.screen instanceof HudEditorScreen) return;
        for (MoveableHud hud : HudRegistry.all()) {
            if (!hud.isEnabled() || !hud.shouldRenderInGame()) continue;
            Rect2i bounds = HudRegistry.bounds(hud, screenWidth, screenHeight);
            hud.render(graphics, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        }
    }
}
