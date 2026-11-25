package com.typeomega.lib.ui;

import com.typeomega.lib.lang.Tr;

/** Texts of the illustrated recipe panels (D-435), declared once for every mod. */
public final class UiTexts {

    public static final Tr TOTAL = Tr.of("typeomegalib.ui.total", "Total: %s EU", "Total: %s EU", "总计：%s EU");
    public static final Tr USAGE = Tr.of("typeomegalib.ui.usage", "Usage: %s EU/t (%s)", "Consumo: %s EU/t (%s)",
            "耗能：%s EU/t（%s）");
    public static final Tr OUTPUT = Tr.of("typeomegalib.ui.output", "Output: %s EU/t (%s)", "Geração: %s EU/t (%s)",
            "产能：%s EU/t（%s）");
    public static final Tr TIME = Tr.of("typeomegalib.ui.time", "Time: %s seconds", "Tempo: %s segundos",
            "耗时：%s 秒");
    public static final Tr CONTINUOUS = Tr.of("typeomegalib.ui.continuous", "Time: runs continuously",
            "Tempo: funciona sem parar", "耗时：持续运行");

    public static final Tr REPORT_SHOW = Tr.of("typeomegalib.ui.report.show", "Show boxes", "Mostrar caixas",
            "显示方框");
    public static final Tr REPORT_COPIED = Tr.of("typeomegalib.ui.report.copied", "Coordinates copied",
            "Coordenadas copiadas", "坐标已复制");

    private UiTexts() {}

    public static void init() {}
}
