package com.typeomega.lib.tooltip.paging;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a tall tooltip into pages. Pure arithmetic over line heights, so it is unit-tested without a client.
 * <p>
 * Line 0 (the item name) is the header and repeats on every page. The body is spread evenly over the fewest pages
 * that fit, breaking at "soft" boundaries (a separator or a section header) when one is close to the even split.
 */
public final class TooltipPager {

    /**
     * One body line.
     *
     * @param height          pixel height, already counting wrapped rows
     * @param softBreakBefore a page may start here (separator, section header, blank line)
     * @param droppable       removed when it lands at the start or end of a page (separators)
     */
    public record Line(int height, boolean softBreakBefore, boolean droppable) {}

    /** Body lines {@code [from, to)}; {@code from..to} index the body list (header excluded). */
    public record Page(int from, int to) {}

    private TooltipPager() {}

    /**
     * @param body     the lines after the header
     * @param capacity pixels available for body lines on one page (screen minus header, footer and padding)
     * @return one page when everything fits, otherwise the pages in order; never empty for a non-empty body
     */
    public static List<Page> paginate(List<Line> body, int capacity) {
        int total = 0;
        for (Line line : body) total += line.height();
        if (total <= capacity || body.isEmpty()) return List.of(new Page(0, body.size()));
        // EN: GTNH-style: the fewest pages that fit, then the lines are spread evenly over them, so no page is left
        // EN: with two lines; a page ends at a section break unless that costs more than a fifth of a page.
        // PT: Estilo GTNH: o menor número de páginas que cabe, e as linhas divididas por igual entre elas, sem página
        // PT: com duas linhas; a página termina numa quebra de seção, salvo se isso custar mais de um quinto da página.
        // ZH: GTNH 风格：取能容纳的最少页数，再把各行平均分配，避免只剩两行的页；
        // ZH: 页面尽量在段落分隔处结束，除非代价超过五分之一页。
        // EN: One extra page is also tried: it wins when the fewest pages would have to cut sections in half.
        // PT: Também testa uma página a mais: ela ganha quando o mínimo de páginas cortaria seções ao meio.
        // ZH: 同时尝试多一页：当最少页数会把段落切半时，多一页更好。
        double extraPage = Math.pow(capacity / 10.0, 2);
        List<Page> best = null;
        double bestCost = Double.POSITIVE_INFINITY;
        int tried = 0;
        for (int count = Math.max(2, (total + capacity - 1) / capacity); count <= body.size() && tried < 2; count++) {
            double[] cost = new double[1];
            List<Page> pages = split(body, capacity, count, total / (double) count, cost);
            if (pages == null) continue;
            double c = cost[0] + tried * extraPage;
            if (c < bestCost) {
                bestCost = c;
                best = pages;
            }
            tried++;
        }
        if (best != null) return best;
        List<Page> pages = new ArrayList<>();
        for (int i = 0; i < body.size(); i++) pages.add(new Page(i, i + 1));
        return pages;
    }

    /** Best split into exactly {@code count} pages, or {@code null} when they cannot hold the body. */
    private static List<Page> split(List<Line> body, int capacity, int count, double target, double[] total) {
        int n = body.size();
        double hardBreak = Math.pow(capacity / 5.0, 2);
        double[][] cost = new double[count + 1][n + 1];
        int[][] from = new int[count + 1][n + 1];
        for (double[] row : cost) java.util.Arrays.fill(row, Double.POSITIVE_INFINITY);
        cost[0][0] = 0;
        for (int p = 1; p <= count; p++) {
            for (int end = 1; end <= n; end++) {
                for (int start = end - 1; start >= p - 1; start--) {
                    if (cost[p - 1][start] == Double.POSITIVE_INFINITY) continue;
                    int[] trimmed = trim(body, start, end);
                    int height = height(body, trimmed[0], trimmed[1]);
                    if (height > capacity && trimmed[1] - trimmed[0] > 1) break;
                    if (trimmed[1] <= trimmed[0]) continue;
                    double c = cost[p - 1][start] + Math.pow(height - target, 2);
                    if (end < n && !body.get(end).softBreakBefore()) c += hardBreak;
                    if (c < cost[p][end]) {
                        cost[p][end] = c;
                        from[p][end] = start;
                    }
                }
            }
        }
        if (cost[count][n] == Double.POSITIVE_INFINITY) return null;
        total[0] = cost[count][n];
        Page[] pages = new Page[count];
        for (int p = count, end = n; p > 0; p--) {
            int start = from[p][end];
            int[] trimmed = trim(body, start, end);
            pages[p - 1] = new Page(trimmed[0], trimmed[1]);
            end = start;
        }
        return List.of(pages);
    }

    private static int[] trim(List<Line> body, int start, int end) {
        while (start < end && body.get(start).droppable()) start++;
        while (end > start + 1 && body.get(end - 1).droppable()) end--;
        return new int[] { start, end };
    }

    private static int height(List<Line> body, int start, int end) {
        int sum = 0;
        for (int i = start; i < end; i++) sum += body.get(i).height();
        return sum;
    }
}
