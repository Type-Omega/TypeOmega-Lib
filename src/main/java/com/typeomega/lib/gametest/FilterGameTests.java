package com.typeomega.lib.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.typeomega.lib.TypeOmegaLib;
import com.typeomega.lib.test.GameTestFilter;
import com.typeomega.lib.test.TestBatches;

/** Verifies that {@link GameTestFilter} and its registry mixin filter GameTests and exclude heavy tests by default. */
@PrefixGameTestTemplate(false)
@GameTestHolder(TypeOmegaLib.MOD_ID)
public final class FilterGameTests {

    private FilterGameTests() {}

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void registeredTestsMatchActiveFilter(GameTestHelper h) {
        h.assertTrue(GameTestFilter.registeredCount() > 0, "GameTestFilter mixin did not observe any registrations");
        GameTestFilter active = GameTestFilter.fromSystemProperty();
        if (active.isActive() && !active.isRunAll()) {
            for (TestFunction fn : GameTestRegistry.getAllTestFunctions()) {
                String compactName = fn.getTestName().replace("_", "");
                boolean matched = active.matches("", fn.getTestName(), fn.getBatchName()) ||
                        active.matches("", compactName, fn.getBatchName()) ||
                        GameTestRegistry.getAllTestClassNames().stream()
                                .anyMatch(cls -> active.matches(cls, fn.getTestName(), fn.getBatchName()));
                h.assertTrue(matched, "Registered test " + fn.getTestName() + " should match active filter");
            }
        }
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void defaultRunExcludesMarkedSlowBatch(GameTestHelper h) {
        GameTestFilter active = GameTestFilter.fromSystemProperty();
        boolean heavyPresent = GameTestRegistry.getAllTestFunctions().stream()
                .anyMatch(fn -> TestBatches.isHeavy(fn.getBatchName()));
        if (active.isHeavyAllowed()) {
            h.assertTrue(heavyPresent, "heavyBatchMarkerTest should be registered when heavy or all is set");
        } else {
            h.assertTrue(!heavyPresent, "heavyBatchMarkerTest must be excluded by default");
            h.assertTrue(GameTestFilter.skippedHeavyCount() >= 1, "at least one heavy test should be skipped");
        }
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", batch = TestBatches.HEAVY, timeoutTicks = 20)
    public static void heavyBatchMarkerTest(GameTestHelper h) {
        h.assertTrue(GameTestFilter.fromSystemProperty().isHeavyAllowed(),
                "heavyBatchMarkerTest must only run when heavy or all is set");
        h.succeed();
    }
}
