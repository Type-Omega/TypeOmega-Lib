package com.typeomega.lib.tooltip.paging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class TooltipPagerTest {

    private static TooltipPager.Line text() {
        return new TooltipPager.Line(10, false, false);
    }

    private static TooltipPager.Line separator() {
        return new TooltipPager.Line(10, true, true);
    }

    @Test
    void shortTooltipStaysOnePage() {
        List<TooltipPager.Line> body = List.of(text(), text(), text());
        assertEquals(List.of(new TooltipPager.Page(0, 3)), TooltipPager.paginate(body, 30));
    }

    @Test
    void longTooltipSplitsAndEveryLineIsShownOnce() {
        List<TooltipPager.Line> body = new ArrayList<>();
        for (int i = 0; i < 25; i++) body.add(text());
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, 100);
        assertEquals(3, pages.size());
        int next = 0;
        for (TooltipPager.Page page : pages) {
            assertEquals(next, page.from());
            assertTrue((page.to() - page.from()) * 10 <= 100);
            next = page.to();
        }
        assertEquals(25, next);
    }

    @Test
    void breaksAtSeparatorAndDropsIt() {
        List<TooltipPager.Line> body = new ArrayList<>();
        for (int i = 0; i < 8; i++) body.add(text());
        body.add(separator());
        for (int i = 0; i < 8; i++) body.add(text());
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, 100);
        assertEquals(new TooltipPager.Page(0, 8), pages.get(0));
        assertEquals(9, pages.get(1).from());
    }

    @Test
    void wrappedLineTallerThanPageStillMakesProgress() {
        List<TooltipPager.Line> body = List.of(new TooltipPager.Line(500, false, false), text());
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, 100);
        assertEquals(2, pages.size());
    }

    @Test
    void earlySeparatorIsIgnoredWhenItWouldWastePage() {
        List<TooltipPager.Line> body = new ArrayList<>();
        body.add(text());
        body.add(separator());
        for (int i = 0; i < 20; i++) body.add(text());
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, 100);
        assertEquals(10, pages.get(0).to() - pages.get(0).from());
    }
}
