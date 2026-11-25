package com.typeomega.lib.tooltip.paging;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Pages item tooltips that are taller than the screen at the current GUI scale (the GTNH multi-page idea). Only items
 * of opted-in namespaces are paged ({@link #enable(String)}); a tooltip that fits is left exactly as it is.
 * <p>
 * Pages break at separators and section headers when possible. The player turns pages with the "Next tooltip page"
 * key (Tab by default; Shift+Tab goes back) or Ctrl+mouse wheel; the item name repeats on every page.
 */
public final class TooltipPaging {

    private static final Set<String> NAMESPACES = new CopyOnWriteArraySet<>(
            List.of("typeomega", "typeomegalib", "gtna", "agronomica"));
    private static final int ROW = 10;
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
        MinecraftForge.EVENT_BUS.addListener(TooltipPaging::onGather);
        MinecraftForge.EVENT_BUS.addListener(TooltipPaging::onKey);
        MinecraftForge.EVENT_BUS.addListener(TooltipPaging::onScroll);
    }

    private static void registerKey(RegisterKeyMappingsEvent event) {
        key = new KeyMapping(PagingTexts.KEY.key(), InputConstants.KEY_TAB, PagingTexts.CATEGORY.key());
        event.register(key);
    }

    private static void onGather(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        if (stack.isEmpty() || elements.size() < 4) return;
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !NAMESPACES.contains(id.getNamespace())) return;
        for (var element : elements) if (element.right().isPresent()) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        List<FormattedText> texts = new ArrayList<>(elements.size());
        for (var element : elements) texts.add(element.left().orElseThrow());

        int wrap = wrapWidth(mc, font, texts, event.getScreenWidth());
        List<TooltipPager.Line> body = new ArrayList<>();
        for (int i = 1; i < texts.size(); i++) body.add(describe(font, texts.get(i), wrap));
        int capacity = event.getScreenHeight() - FRAME - HEADER - FOOTER;
        List<TooltipPager.Page> pages = TooltipPager.paginate(body, capacity);
        if (pages.size() < 2) {
            pageCount = 1;
            return;
        }

        Object identity = List.of(stack.getItem(), elements.size(), pages.size());
        if (!identity.equals(lastKey)) {
            lastKey = identity;
            page = 0;
        }
        pageCount = pages.size();
        page = Math.floorMod(page, pageCount);
        lastShownMs = System.currentTimeMillis();

        TooltipPager.Page shown = pages.get(page);
        List<Either<FormattedText, TooltipComponent>> out = new ArrayList<>();
        out.add(elements.get(0));
        for (int i = shown.from(); i < shown.to(); i++) out.add(elements.get(i + 1));
        out.add(Either.left(PagingTexts.PAGE.component(page + 1, pageCount).withStyle(ChatFormatting.AQUA)));
        Component keyName = key == null ? Component.literal("Tab") : key.getTranslatedKeyMessage();
        out.add(Either.left(PagingTexts.HINT.component(keyName, keyName).withStyle(ChatFormatting.DARK_GRAY)));
        elements.clear();
        elements.addAll(out);
    }

    // EN: Same rule as Forge's tooltip gatherer: wrap only when the box would not fit left or right of the mouse.
    // PT: Mesma regra do Forge: só quebra linha quando a caixa não cabe à esquerda nem à direita do mouse.
    // ZH: 与 Forge 的提示框规则相同：仅当提示框在鼠标左右两侧都放不下时才换行。
    private static int wrapWidth(Minecraft mc, Font font, List<FormattedText> texts, int screenWidth) {
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
