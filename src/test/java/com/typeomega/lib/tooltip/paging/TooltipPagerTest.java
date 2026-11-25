package com.typeomega.lib.tooltip.paging;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void oversizedSectionIsSplitAndTheShortOneStaysWhole() {
        List<TooltipPager.Line> body = new ArrayList<>();
        body.add(text());
        body.add(separator());
        for (int i = 0; i < 20; i++) body.add(text());
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, 100);
        assertEquals(new TooltipPager.Page(0, 1), pages.get(0));
        for (TooltipPager.Page page : pages) assertTrue((page.to() - page.from()) * 10 <= 100);
    }

    @Test
    void eachPageHoldsWholeSections() {
        List<TooltipPager.Line> body = new ArrayList<>();
        int[] sizes = { 4, 7, 3, 8, 5 };
        for (int s = 0; s < sizes.length; s++) {
            if (s > 0) body.add(separator());
            for (int i = 0; i < sizes[s]; i++) body.add(text());
        }
        for (TooltipPager.Page page : TooltipPager.paginate(body, 100)) {
            boolean startsSection = page.from() == 0 || body.get(page.from() - 1).softBreakBefore();
            boolean endsSection = page.to() == body.size() || body.get(page.to()).softBreakBefore();
            assertTrue(startsSection && endsSection, "page " + page + " cuts a section");
        }
    }

    @Test
    void pagesAreEvenWithNoNearlyEmptyLastPage() {
        List<TooltipPager.Line> body = new ArrayList<>();
        for (int i = 0; i < 21; i++) body.add(text());
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, 100);
        assertEquals(3, pages.size());
        for (TooltipPager.Page page : pages) assertEquals(7, page.to() - page.from());
    }

    @Test
    void wholeSectionsStayTogetherWhenTheyFit() {
        List<TooltipPager.Line> body = new ArrayList<>();
        for (int s = 0; s < 3; s++) {
            if (s > 0) body.add(separator());
            for (int i = 0; i < 6; i++) body.add(text());
        }
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, 100);
        assertEquals(3, pages.size());
        for (TooltipPager.Page page : pages) assertEquals(6, page.to() - page.from(), "a page cuts a section");
    }
}
