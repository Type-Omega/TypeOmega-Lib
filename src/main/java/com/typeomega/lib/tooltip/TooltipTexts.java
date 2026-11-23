package com.typeomega.lib.tooltip;

import com.typeomega.lib.lang.Tr;

/** Shared texts of the GTNH-style multiblock tooltip (GTIA-D-410), declared once for every mod. */
final class TooltipTexts {

    static final Tr MACHINE_TYPE = Tr.of("typeomegalib.tooltip.machine_type", "Machine Type: %s",
            "Tipo de Máquina: %s", "机器类型：%s");
    static final Tr SUPPORTS = Tr.of("typeomegalib.tooltip.supports", "Supports %s!", "Suporta %s!", "支持%s！");
    static final Tr AND = Tr.of("typeomegalib.tooltip.list.and", "%s and %s", "%s e %s", "%s和%s");
    static final Tr STRUCTURE_HINT = Tr.of("typeomegalib.tooltip.structure_hint",
            "Hold %s to display structure guidelines", "Segure %s para ver as instruções da estrutura",
            "按住%s查看结构指引");
    static final Tr SHIFT_KEY = Tr.of("typeomegalib.tooltip.key.shift", "[LSHIFT]", "[LSHIFT]", "[LSHIFT]");

    private TooltipTexts() {}
}
