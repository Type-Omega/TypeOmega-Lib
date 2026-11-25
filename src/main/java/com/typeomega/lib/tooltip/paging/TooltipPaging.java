package com.typeomega.lib.tooltip.paging;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Pages item tooltips that are taller than the screen at the current GUI scale, or longer than 20 rows (the GTNH
 * multi-page idea). Only items
 * of opted-in namespaces are paged ({@link #enable(String)}); a tooltip that fits is left exactly as it is.
 * <p>
 * Pages break at separators and section headers when possible. The player turns pages with the "Next tooltip page"
 * key (Tab by default; Shift+Tab goes back) or Ctrl+mouse wheel; the item name repeats on every page.
 */
public final class TooltipPaging {

    private static final Set<String> NAMESPACES = new CopyOnWriteArraySet<>(
            List.of("typeomega", "typeomegalib", "gtna", "agronomica"));
    private static final int ROW = 10;
    private static final int MAX_PAGE_ROWS = 20;
    // EN: Vanilla draws 3px of padding above and below, and shifts the box to keep 4px from the edge.
    // PT: O vanilla desenha 3px de margem acima e abaixo e mantém 4px da borda da tela.
    // ZH: 原版上下各有3像素内边距，并与屏幕边缘保持4像素。
    private static final int FRAME = 6 + 8;
    private static final int HEADER = ROW + 2;
    private static final int FOOTER = 2 * ROW + 2;

    private static KeyMapping key;
    private static int page;
    private static int pageCount = 1;
    private static Object lastKey;
    private static long lastShownMs;

    private TooltipPaging() {}

    /** Opts the items of a mod namespace in to tooltip paging. */
    public static void enable(String namespace) {
        NAMESPACES.add(namespace);
    }

    /** Called once from the lib constructor on the client only. */
    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TooltipPaging::registerKey);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, TooltipPaging::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(TooltipPaging::onKey);
        MinecraftForge.EVENT_BUS.addListener(TooltipPaging::onScroll);
    }

    private static void registerKey(RegisterKeyMappingsEvent event) {
        key = new KeyMapping(PagingTexts.KEY.key(), InputConstants.KEY_TAB, PagingTexts.CATEGORY.key());
        event.register(key);
    }

    // EN: Paged here, not in GatherComponents: EMI's item list draws ItemStack#getTooltipLines without firing that
    // EN: event, so a tall tooltip hovered in EMI was never paged. Only on the render thread with a screen open, so
    // EN: EMI's search index (its own thread) still sees every line.
    // PT: Paginado aqui, não no GatherComponents: a lista de itens do EMI desenha ItemStack#getTooltipLines sem
    // PT: disparar esse evento, então tooltip alta vista no EMI nunca paginava. Só na thread de render com tela
    // PT: aberta, para o índice de busca do EMI (thread própria) continuar vendo todas as linhas.
    // ZH: 在此分页而非 GatherComponents：EMI 物品列表绘制 ItemStack#getTooltipLines 时不触发该事件，
    // ZH: 因此在 EMI 中悬停的长提示从未分页。仅在渲染线程且打开界面时分页，EMI 搜索索引（独立线程）仍能看到全部行。
    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (stack.isEmpty() || id == null || !NAMESPACES.contains(id.getNamespace())) return;
        List<Component> lines = event.getToolTip();
        boolean emiAppends = EmiModLine.dropIfEmiAppends(lines, id.getNamespace());
        Minecraft mc = Minecraft.getInstance();
        if (lines.size() < 4 || mc.screen == null || !mc.isSameThread()) return;

        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int wrap = wrapWidth(mc, font, lines, screenWidth);
        List<TooltipPager.Line> body = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) body.add(describe(font, lines.get(i), wrap));
        // EN: Long tooltips are paged even on a tall screen, like GTNH: a page shows at most MAX_PAGE_ROWS rows.
        // PT: Tooltips longas são paginadas mesmo em tela alta, como no GTNH: a página mostra no máximo MAX_PAGE_ROWS
        // PT: linhas.
        // ZH: 与 GTNH 一样，即使屏幕足够高，长提示也会分页：每页最多显示 MAX_PAGE_ROWS 行。
        int capacity = Math.min(MAX_PAGE_ROWS * ROW, screenHeight - FRAME - HEADER - FOOTER - (emiAppends ? ROW : 0));
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, capacity);
        if (pages.size() < 2) {
            pageCount = 1;
            return;
        }

        Object identity = List.of(stack.getItem(), lines.size(), pages.size());
        if (!identity.equals(lastKey)) {
            lastKey = identity;
            page = 0;
        }
        pageCount = pages.size();
        page = Math.floorMod(page, pageCount);
        lastShownMs = System.currentTimeMillis();

        TooltipPager.Page shown = pages.get(page);
        List<Component> out = new ArrayList<>();
        out.add(lines.get(0));
        for (int i = shown.from(); i < shown.to(); i++) out.add(lines.get(i + 1));
        out.add(PagingTexts.PAGE.component(page + 1, pageCount).withStyle(ChatFormatting.AQUA));
        Component keyName = key == null ? Component.literal("Tab") : key.getTranslatedKeyMessage();
        out.add(PagingTexts.HINT.component(keyName, keyName).withStyle(ChatFormatting.DARK_GRAY));
        lines.clear();
        lines.addAll(out);
    }

    // EN: Same rule as Forge's tooltip gatherer: wrap only when the box would not fit left or right of the mouse.
    // PT: Mesma regra do Forge: só quebra linha quando a caixa não cabe à esquerda nem à direita do mouse.
    // ZH: 与 Forge 的提示框规则相同：仅当提示框在鼠标左右两侧都放不下时才换行。
    private static int wrapWidth(Minecraft mc, Font font, List<? extends FormattedText> texts, int screenWidth) {
        int widest = 0;
        for (FormattedText text : texts) widest = Math.max(widest, font.width(text));
        double scale = (double) mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        int mouseX = (int) (mc.mouseHandler.xpos() * scale);
        int x = mouseX + 12;
        if (x + widest + 4 > screenWidth) {
            x = mouseX - 16 - widest;
            if (x < 4) return mouseX > screenWidth / 2 ? mouseX - 12 - 8 : screenWidth - 16 - mouseX;
        }
        return widest;
    }

    private static TooltipPager.Line describe(Font font, FormattedText text, int wrap) {
        String plain = text.getString();
        int rows = Math.max(1, font.split(text, Math.max(wrap, 10)).size());
        boolean separator = !plain.isEmpty() && plain.chars().allMatch(c -> c == '─');
        boolean header = plain.startsWith("- ") && isGold(text);
        boolean blank = plain.isBlank();
        return new TooltipPager.Line(rows * ROW, separator || header || blank, separator || blank);
    }

    private static boolean isGold(FormattedText text) {
        if (!(text instanceof Component component)) return false;
        Style style = component.getStyle();
        return style.getColor() != null && style.getColor().getValue() == ChatFormatting.GOLD.getColor();
    }

    private static boolean showing() {
        return pageCount > 1 && System.currentTimeMillis() - lastShownMs < 250;
    }

    private static void turn(int delta) {
        page = Math.floorMod(page + delta, pageCount);
    }

    private static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (key == null || !showing() || !key.matches(event.getKeyCode(), event.getScanCode())) return;
        turn(Screen.hasShiftDown() ? -1 : 1);
        event.setCanceled(true);
    }

    private static void onScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!showing() || !Screen.hasControlDown() || event.getScrollDelta() == 0) return;
        turn(event.getScrollDelta() > 0 ? -1 : 1);
        event.setCanceled(true);
    }
}
