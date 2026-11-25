package com.typeomega.lib.tooltip.paging;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a tall tooltip into pages. Pure arithmetic over line heights, so it is unit-tested without a client.
 * <p>
 * Line 0 (the item name) is the header and repeats on every page. Pages break at "soft" boundaries (a separator or a
 * section header) when one exists in the page; otherwise they break where the next line no longer fits.
 */
public final class TooltipPager {

    /**
     * One body line.
     *
     * @param height pixel height, already counting wrapped rows
     * @param softBreakBefore a page may start here (separator, section header, blank line)
     * @param droppable removed when it lands at the start or end of a page (separators)
     */
    public record Line(int height, boolean softBreakBefore, boolean droppable) {}

    /** Body lines {@code [from, to)}; {@code from..to} index the body list (header excluded). */
    public record Page(int from, int to) {}

    private TooltipPager() {}

    /**
     * @param body the lines after the header
     * @param capacity pixels available for body lines on one page (screen minus header, footer and padding)
     * @return one page when everything fits, otherwise the pages in order; never empty for a non-empty body
     */
    public static List<Page> paginate(List<Line> body, int capacity) {
        List<Page> pages = new ArrayList<>();
        int total = 0;
        for (Line line : body) total += line.height();
        if (total <= capacity || body.isEmpty()) {
            pages.add(new Page(0, body.size()));
            return pages;
        }
        // EN: A page gives up to a third of its height to reach a soft break; below that a hard break is better.
        // PT: Uma página abre mão de até um terço da altura para chegar a uma quebra suave; abaixo disso, quebra dura.
        // ZH: 页面最多让出三分之一的高度来对齐软分页点；再少则直接硬分页。
        int start = skipDroppable(body, 0);
        while (start < body.size()) {
            int used = 0;
            int end = start;
            while (end < body.size() && (end == start || used + body.get(end).height() <= capacity)) {
                used += body.get(end).height();
                end++;
            }
            if (end < body.size()) {
                int minimum = start + 1;
                int floorPx = capacity * 2 / 3;
                int acc = 0;
                int best = -1;
                for (int i = start; i < end; i++) {
                    if (i > start && body.get(i).softBreakBefore() && acc >= floorPx) best = i;
                    acc += body.get(i).height();
                }
                if (best >= minimum) end = best;
            }
            int to = end;
            while (to > start + 1 && body.get(to - 1).droppable()) to--;
            pages.add(new Page(start, to));
            start = skipDroppable(body, end);
        }
        if (pages.isEmpty()) pages.add(new Page(0, body.size()));
        return pages;
    }

    private static int skipDroppable(List<Line> body, int index) {
        while (index < body.size() && body.get(index).droppable()) index++;
        return index;
    }
}
