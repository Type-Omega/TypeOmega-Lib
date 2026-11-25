package com.typeomega.lib.ui.hud;

import com.typeomega.lib.lang.Tr;

/** Texts of the shared HUD editor and its key binding, declared once for every mod. */
public final class HudTexts {

    public static final String KEY_CATEGORY = "typeomegalib.key.category";

    public static final Tr EDITOR_TITLE = Tr.of("typeomegalib.hud.editor.title",
            "HUD Editor",
            "Editor de HUD",
            "HUD编辑器");
    public static final Tr EDITOR_HINT = Tr.of("typeomegalib.hud.editor.hint",
            "Drag any HUD to move it; positions are saved automatically. ESC closes.",
            "Arraste um HUD para mover; as posições são salvas automaticamente. ESC fecha.",
            "拖动HUD即可移动，位置会自动保存。按ESC关闭。");
    public static final Tr EDITOR_ON = Tr.of("typeomegalib.hud.editor.on", "ON", "LIGADO", "开");
    public static final Tr EDITOR_OFF = Tr.of("typeomegalib.hud.editor.off", "OFF", "DESLIGADO", "关");
    public static final Tr KEY_EDITOR = Tr.of("typeomegalib.key.hud_editor",
            "Open HUD Editor",
            "Abrir Editor de HUD",
            "打开HUD编辑器");

    private HudTexts() {}

    /** Declares the texts so datagen sees them on every side. */
    public static void init() {}
}
