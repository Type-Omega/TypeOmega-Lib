package com.typeomega.lib.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.typeomega.lib.TypeOmegaLib;
import com.typeomega.lib.test.GameTestFilter;

/** Verifies that {@link GameTestFilter} and its registry mixin filter GameTests as configured. */
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
                boolean matched = active.matches("", fn.getTestName(), fn.getBatchName()) ||
                        GameTestRegistry.getAllTestClassNames().stream()
                                .anyMatch(cls -> active.matches(cls, fn.getTestName(), fn.getBatchName()));
                h.assertTrue(matched, "Registered test " + fn.getTestName() + " should match active filter");
            }
        }
        h.succeed();
    }
}
