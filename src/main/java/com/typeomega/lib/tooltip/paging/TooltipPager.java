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
        // EN: GTNH-style: whole sections per page, on as few pages as possible, as even as the sections allow.
        // PT: Estilo GTNH: seções inteiras por página, no menor número de páginas, o mais parelhas que as seções
        // permitem.
        // ZH: GTNH 风格：每页放完整段落，页数尽量少，并在段落允许的范围内尽量均匀。
        // EN: One extra page is also tried: it wins when the fewest pages would have to cut sections in half.
        // PT: Também testa uma página a mais: ela ganha quando o mínimo de páginas cortaria seções ao meio.
        // ZH: 同时尝试多一页：当最少页数会把段落切半时，多一页更好。
        double extraPage = Math.pow(capacity / 10.0, 2);
        List<Page> best = null;
        double bestCost = Double.POSITIVE_INFINITY;
        int tried = 0;
        for (int count = Math.max(2, (total + capacity - 1) / capacity); count <= body.size() &&
                (tried < 2 || bestCost >= hardLimit(capacity, body.size())); count++) {
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
        // EN: As in GTNH each page is its own topic: a page never ends inside a section unless that section alone is
        // EN: taller than the page.
        // PT: Como no GTNH cada página é um assunto: a página nunca termina no meio de uma seção, salvo se a seção
        // PT: sozinha for mais alta que a página.
        // ZH: 与 GTNH 一样每页是一个主题：除非某段本身比一页还高，否则页面绝不在段落中间结束。
        double hardBreak = hardLimit(capacity, n);
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

    /** Cost of one break inside a section; any split without one costs less. */
    private static double hardLimit(int capacity, int lines) {
        return 1000.0 * capacity * capacity * lines;
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
