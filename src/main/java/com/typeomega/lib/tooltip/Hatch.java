package com.typeomega.lib.tooltip;

import com.typeomega.lib.lang.Tr;

/** Special hatches a multiblock accepts, listed in the green "Supports ...!" line. */
public enum Hatch {

    MULTI_AMP(Tr.of("typeomegalib.tooltip.hatch.multi_amp", "Multi-Amp", "Multi-Amp", "多安")),
    LASER(Tr.of("typeomegalib.tooltip.hatch.laser", "Laser Hatches", "Escotilhas Laser", "激光仓")),
    WIRELESS(Tr.of("typeomegalib.tooltip.hatch.wireless", "Wireless Hatches", "Escotilhas sem Fio", "无线仓"));

    private final Tr name;

    Hatch(Tr name) {
        this.name = name;
    }

    public Tr text() {
        return name;
    }
}
