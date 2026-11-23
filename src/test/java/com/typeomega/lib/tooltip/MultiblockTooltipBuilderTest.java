package com.typeomega.lib.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;

import com.typeomega.lib.lang.Tr;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiblockTooltipBuilderTest {

    private static final Tr TYPE = Tr.of("test.type.collider", "Plasma Collider", "Colisor", "等离子对撞机");
    private static final Tr DRAIN = Tr.of("test.line.drain", "Set %s in the Controller", "Ajuste %s", "设置%s");
    private static final Tr RATE = Tr.of("test.word.rate", "Drain Rate", "Taxa", "消耗速率");
    private static final Tr FORCE = Tr.of("test.mat.force", "Force", "Force", "力量");
    private static final Tr BUFF = Tr.of("test.effect.buff", "+%s%% EU/mB", "+%s%% EU/mB", "+%s%% EU/mB");
    private static final Tr NOT_RETURNED = Tr.of("test.warn.molten", "Plasmas are %s returned", "%s volta",
            "%s返还");
    private static final Tr NOT = Tr.of("test.word.not", "NOT", "NÃO", "不");

    private static List<Component> full() {
        return MultiblockTooltip.builder()
                .machineType(TYPE, TYPE)
                .line(DRAIN, TooltipArg.configurable(RATE))
                .separator()
                .table(TooltipRow.of(FORCE, ChatFormatting.GOLD, 10_000, 200_000L).effect(BUFF, 20))
                .separator()
                .line(NOT_RETURNED, TooltipArg.emphasis(NOT))
                .hatches(Hatch.MULTI_AMP, Hatch.LASER, Hatch.WIRELESS)
                .humor(TYPE)
                .structureHint()
                .mod("GregTech Nexus Addon")
                .build();
    }

    private static String key(Component c) {
        return ((TranslatableContents) c.getContents()).getKey();
    }

    private static Object[] args(Component c) {
        return ((TranslatableContents) c.getContents()).getArgs();
    }

    @Test
    void keepsGtnhOrder() {
        List<Component> lines = full();
        assertEquals(11, lines.size());
        assertEquals("typeomegalib.tooltip.machine_type", key(lines.get(0)));
        assertEquals("test.line.drain", key(lines.get(1)));
        assertEquals(MultiblockTooltipBuilder.SEPARATOR, lines.get(2).getString());
        assertEquals("typeomegalib.tooltip.supports", key(lines.get(6)));
        assertEquals("typeomegalib.tooltip.structure_hint", key(lines.get(9)));
        Component mod = lines.get(10);
        assertEquals("GregTech Nexus Addon", mod.getString());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.BLUE), mod.getStyle().getColor());
        assertTrue(mod.getStyle().isItalic());
    }

    @Test
    void machineTypesAreYellowAndTranslatable() {
        Component typeArg = (Component) args(full().get(0))[0];
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.YELLOW), typeArg.getStyle().getColor());
        assertEquals(2, typeArg.getSiblings().stream().filter(c -> c.getContents() instanceof TranslatableContents)
                .count());
    }

    @Test
    void argumentsKeepColours() {
        Component configurable = (Component) args(full().get(1))[0];
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GREEN), configurable.getStyle().getColor());
        Component not = (Component) args(full().get(5))[0];
        assertTrue(not.getStyle().isBold());
        assertEquals("test.word.not", key(not));
    }

    @Test
    void tableRowFormatsNumbersFromDefinition() {
        Component row = full().get(3);
        List<Component> parts = row.getSiblings();
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GOLD), parts.get(0).getStyle().getColor());
        assertEquals("10,000", parts.get(2).getString());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GREEN), parts.get(2).getStyle().getColor());
        assertEquals("200,000", parts.get(4).getString());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.AQUA), parts.get(4).getStyle().getColor());
        assertEquals("test.effect.buff", key(parts.get(6)));
        assertEquals(20, args(parts.get(6))[0]);
    }

    @Test
    void hatchListUsesTranslatedConjunction() {
        Component list = (Component) args(full().get(6))[0];
        assertEquals("typeomegalib.tooltip.list.and", key(list));
    }

    @Test
    void humorIsOptional() {
        List<Component> lines = MultiblockTooltip.builder().machineType(TYPE).structureHint().mod("M").build();
        assertEquals(4, lines.size());
    }

    @Test
    void rejectsWrongOrder() {
        assertThrows(IllegalStateException.class, () -> MultiblockTooltip.builder().line(DRAIN).machineType(TYPE));
        assertThrows(IllegalStateException.class, () -> MultiblockTooltip.builder().machineType(TYPE).build());
        assertThrows(IllegalStateException.class,
                () -> MultiblockTooltip.builder().machineType(TYPE).mod("M").line(DRAIN));
        assertThrows(IllegalStateException.class, () -> MultiblockTooltip.builder().mod("M").build());
    }

    @Test
    void arrayFormForGtceu() {
        assertEquals(11, MultiblockTooltip.builder().machineType(TYPE).line(DRAIN).separator()
                .table(TooltipRow.of(FORCE, ChatFormatting.GOLD, 1)).separator().line(DRAIN).hatches(Hatch.LASER)
                .humor(TYPE).structureHint().mod("M").toArray().length);
    }
}
