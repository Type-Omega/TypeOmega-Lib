package com.typeomega.lib.tooltip.paging;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Drops the mod-name line that GTNH-style tooltips end with when EMI is going to append the same line itself, so
 * the name is not shown twice.
 */
final class EmiModLine {

    private static final Field APPEND_MOD_ID = field("appendModId");
    private static final Field APPEND_ITEM_MOD_ID = field("appendItemModId");

    private EmiModLine() {}

    // EN: EMI appends the mod name after ItemTooltipEvent (a mixin at the return of getTooltipLines and again in its
    // EN: list) when both options are on, as in the pack; then our own last line is a duplicate.
    // PT: O EMI acrescenta o nome do mod depois do ItemTooltipEvent (mixin no retorno de getTooltipLines e de novo
    // PT: na lista) quando as duas opções estão ligadas, como no pack; aí a nossa última linha fica duplicada.
    // ZH: 当两个选项都开启时（整合包即如此），EMI 会在 ItemTooltipEvent 之后追加模组名
    // ZH: （getTooltipLines 返回处的 mixin 及其列表中），此时我们自己的最后一行就重复了。
    static void dropIfEmiAppends(List<Component> lines, String namespace) {
        if (lines.size() < 2 || !enabled(APPEND_MOD_ID) || !enabled(APPEND_ITEM_MOD_ID)) return;
        Component last = lines.get(lines.size() - 1);
        Style style = last.getStyle();
        if (!style.isItalic() || style.getColor() == null ||
                style.getColor().getValue() != ChatFormatting.BLUE.getColor()) {
            return;
        }
        String name = ModList.get().getModContainerById(namespace)
                .map(c -> c.getModInfo().getDisplayName()).orElse(null);
        if (last.getString().equals(name)) lines.remove(lines.size() - 1);
    }

    private static boolean enabled(Field field) {
        try {
            return field != null && field.getBoolean(null);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private static Field field(String name) {
        try {
            return Class.forName("dev.emi.emi.config.EmiConfig").getField(name);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }
}
