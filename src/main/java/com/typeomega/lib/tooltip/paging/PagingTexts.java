package com.typeomega.lib.tooltip.paging;

import com.typeomega.lib.lang.Tr;

/** Texts of the paged tooltip footer and its key binding. */
public final class PagingTexts {

    static final Tr PAGE = Tr.of("typeomegalib.tooltip.page", "Page %s/%s", "Página %s/%s", "第%s/%s页");
    static final Tr HINT = Tr.of("typeomegalib.tooltip.page.hint", "[%s] next, Shift+[%s] back, Ctrl+Scroll",
            "[%s] avança, Shift+[%s] volta, Ctrl+Roda", "[%s] 下一页，Shift+[%s] 上一页，Ctrl+滚轮");
    static final Tr KEY = Tr.of("typeomegalib.key.tooltip_page", "Next tooltip page", "Próxima página da tooltip",
            "下一页提示");
    static final Tr CATEGORY = Tr.of("typeomegalib.key.category", "TypeOmega Lib", "TypeOmega Lib", "TypeOmega Lib");

    /** Declares the texts so datagen sees them on every side. */
    public static void init() {
        PAGE.key();
        HINT.key();
        KEY.key();
        CATEGORY.key();
    }

    private PagingTexts() {}
}
