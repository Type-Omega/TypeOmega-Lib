package com.typeomega.lib.tooltip;

import com.typeomega.lib.lang.Tr;

/** Shared texts of the GTNH-style multiblock tooltip (D-410), declared once for every mod. */
final class TooltipTexts {

    static final Tr MACHINE_TYPE = Tr.of("typeomegalib.tooltip.machine_type", "Machine Type: %s",
            "Tipo de Máquina: %s", "机器类型：%s");
    static final Tr SUPPORTS = Tr.of("typeomegalib.tooltip.supports", "Supports %s!", "Suporta %s!", "支持%s！");
    static final Tr AND = Tr.of("typeomegalib.tooltip.list.and", "%s and %s", "%s e %s", "%s和%s");

    private TooltipTexts() {}
}
