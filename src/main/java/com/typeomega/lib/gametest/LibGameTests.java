package com.typeomega.lib.gametest;

import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.typeomega.lib.TypeOmegaLib;
import com.typeomega.lib.lang.Tr;
import com.typeomega.lib.test.Sweeps;
import com.typeomega.lib.tooltip.MultiblockTooltip;
import com.typeomega.lib.tooltip.StructureInfo;
import com.typeomega.lib.tooltip.structure.GtceuPatterns;

/** Checks the library against itself and against GTCEu's own content. */
@PrefixGameTestTemplate(false)
@GameTestHolder(TypeOmegaLib.MOD_ID)
public final class LibGameTests {

    private LibGameTests() {}

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void translationsNeedAllThreeLanguages(GameTestHelper h) {
        boolean rejected = false;
        try {
            Tr.of("typeomegalib.test.missing", "Text", "Texto", " ");
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        h.assertTrue(rejected, "a translation without Chinese text was accepted");
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void aKeyCannotBeRedeclaredWithOtherText(GameTestHelper h) {
        Tr.of("typeomegalib.test.same", "One", "Um", "一");
        h.assertTrue(Tr.of("typeomegalib.test.same", "One", "Um", "一").text(Tr.PT).equals("Um"),
                "the same declaration twice should return the first one");
        boolean rejected = false;
        try {
            Tr.of("typeomegalib.test.same", "Two", "Dois", "二");
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        h.assertTrue(rejected, "a key was redeclared with different text");
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void multiblockTooltipFollowsTheGtnaLayout(GameTestHelper h) {
        Component[] lines = MultiblockTooltip.of("typeomegalib", "test_machine")
                .fact("A fact", "Um fato", "一个事实")
                .main("Does a thing", "Faz uma coisa", "做一件事")
                .requirement("Needs power", "Precisa de energia", "需要能量")
                .source("GTNL")
                .build();
        h.assertTrue(lines.length == 6, "expected fact + 2 headers + 2 items + source, got " + lines.length);
        h.assertTrue(lines[1].getString().startsWith("- "), "section header should start with '- '");
        h.assertTrue(lines[2].getString().startsWith("∘ "), "section item should start with '∘ '");
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void libraryNamesExistInAllThreeLanguages(GameTestHelper h) {
        Sweeps.namesInAllLanguages(TypeOmegaLib.MOD_ID).assertClean(h, 4);
        h.succeed();
    }

    /** The preview sweep is checked on GTCEu's own multiblocks, which must all form. */
    @GameTest(template = "empty_16", timeoutTicks = 6000)
    public static void gtceuMultiblocksFormFromTheirPreview(GameTestHelper h) {
        Sweeps.multiblocksFormFromPreview(h, "gtceu", 40).assertClean(h, 20);
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void gtceuMachineTooltipsBuild(GameTestHelper h) {
        Sweeps.machineTooltipsBuild("gtceu").assertClean(h, 100);
        h.succeed();
    }

    /**
     * The structure section reads GTCEu's Electric Blast Furnace pattern: 3x4x3, 16 heating coils that accept every
     * tier, one muffler in its own position and 1-2 energy hatches among the heatproof casings.
     */
    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void structureSectionCountsAGtceuPattern(GameTestHelper h) {
        StructureInfo info = GtceuPatterns.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE);
        String size = info.width().text() + "x" + info.height().text() + "x" + info.depth().text();
        h.assertTrue(size.equals("3x4x3"), "EBF dimensions read as " + size);
        h.assertTrue(info.controller() != null, "EBF controller position missing");
        StructureInfo.Block coils = info.blocks().stream().filter(StructureInfo.Block::tiered).findFirst()
                .orElse(null);
        h.assertTrue(coils != null && coils.count().text().equals("16"), "EBF coils should be 16 tiered blocks");
        StructureInfo.Block casing = info.blocks().stream().filter(b -> !b.tiered()).findFirst().orElse(null);
        h.assertTrue(casing != null && casing.count().min() == 9, "EBF casings should start at the pattern minimum 9");
        StructureInfo.Part muffler = info.parts().stream().filter(p -> p.where().isEmpty()).findFirst().orElse(null);
        h.assertTrue(muffler != null && muffler.count().text().equals("1"), "EBF muffler should be 1 own position");
        h.assertTrue(info.abilities().contains("input_energy"), "EBF energy hatch ability not found");
        StructureInfo.Part energy = info.parts().stream().filter(p -> !p.where().isEmpty() &&
                p.count().text().equals("1-2")).findFirst().orElse(null);
        h.assertTrue(energy != null, "EBF energy hatch should be 1-2 in the casings");
        h.succeed();
    }
}
