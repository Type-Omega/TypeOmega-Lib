package com.typeomega.lib.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTestFilterTest {

    @Test
    void nullOrBlankFilterIsInactiveAndMatchesNormalTests() {
        for (String raw : new String[] { null, "", "   ", " , , " }) {
            GameTestFilter filter = GameTestFilter.parse(raw);
            assertFalse(filter.isActive());
            assertFalse(filter.isHeavyAllowed());
            assertEquals(List.of(), filter.terms());
            assertTrue(filter.matches("LibGameTests", "someTest", TestBatches.DEFAULT));
        }
    }

    @Test
    void matchesMethodClassAndBatchCaseInsensitively() {
        GameTestFilter byMethod = GameTestFilter.parse("NanoForge");
        assertTrue(byMethod.matches("MachineTests", "nanoforgeTierFour", TestBatches.DEFAULT));
        assertFalse(byMethod.matches("MachineTests", "boilerForms", TestBatches.DEFAULT));

        GameTestFilter byClass = GameTestFilter.parse("TOOLTIP");
        assertTrue(byClass.matches("TooltipGameTests", "checksLines", TestBatches.DEFAULT));
        assertFalse(byClass.matches("RecipeGameTests", "checksLines", TestBatches.DEFAULT));

        GameTestFilter byBatch = GameTestFilter.parse("Smoke");
        assertTrue(byBatch.matches("OtherTests", "runs", "smokeBatch"));
        assertFalse(byBatch.matches("OtherTests", "runs", TestBatches.DEFAULT));
    }

    @Test
    void commaSeparatedTermsMatchAnyAndIgnoreWhitespace() {
        GameTestFilter filter = GameTestFilter.parse(" nanoforge , tooltip , , ");
        assertEquals(List.of("nanoforge", "tooltip"), filter.terms());
        assertTrue(filter.matches("NanoForgeTests", "a", TestBatches.DEFAULT));
        assertTrue(filter.matches("OtherTests", "tooltipCheck", TestBatches.DEFAULT));
        assertFalse(filter.matches("BoilerTests", "boils", TestBatches.DEFAULT));
    }

    @Test
    void heavyBatchAndHeavyAnnotationAreExcludedByDefaultAndIncludedByHeavyOrAll() throws NoSuchMethodException {
        Method batchHeavy = SampleTests.class.getDeclaredMethod("heavyByBatch", GameTestHelper.class);
        Method methodHeavy = SampleTests.class.getDeclaredMethod("heavyByAnnotation", GameTestHelper.class);
        Method classHeavy = HeavyClassTests.class.getDeclaredMethod("insideHeavyClass", GameTestHelper.class);
        Method normal = SampleTests.class.getDeclaredMethod("normalNanoForge", GameTestHelper.class);

        for (String raw : new String[] { null, "", "nanoforge", "SampleTests" }) {
            GameTestFilter filter = GameTestFilter.parse(raw);
            assertFalse(filter.isHeavyAllowed(), "raw=" + raw);
            assertFalse(filter.matchesMethod(batchHeavy), "raw=" + raw);
            assertFalse(filter.matchesMethod(methodHeavy), "raw=" + raw);
            assertFalse(filter.matchesMethod(classHeavy), "raw=" + raw);
        }

        GameTestFilter onlyHeavy = GameTestFilter.parse("HEAVY");
        assertTrue(onlyHeavy.isHeavyAllowed());
        assertTrue(onlyHeavy.matchesMethod(batchHeavy));
        assertTrue(onlyHeavy.matchesMethod(methodHeavy));
        assertTrue(onlyHeavy.matchesMethod(classHeavy));
        assertFalse(onlyHeavy.matchesMethod(normal));

        GameTestFilter all = GameTestFilter.parse("ALL");
        assertTrue(all.isRunAll());
        assertTrue(all.isHeavyAllowed());
        assertTrue(all.matchesMethod(batchHeavy));
        assertTrue(all.matchesMethod(methodHeavy));
        assertTrue(all.matchesMethod(classHeavy));
        assertTrue(all.matchesMethod(normal));
    }

    @Test
    void readsFromSystemProperty() {
        String previous = System.getProperty(GameTestFilter.PROPERTY);
        try {
            System.setProperty(GameTestFilter.PROPERTY, "data,module");
            GameTestFilter filter = GameTestFilter.fromSystemProperty();
            assertEquals(List.of("data", "module"), filter.terms());
            assertTrue(filter.matches("DataGameTests", "int128", TestBatches.DEFAULT));
            assertFalse(filter.matches("LibGameTests", "other", TestBatches.DEFAULT));
        } finally {
            if (previous == null) {
                System.clearProperty(GameTestFilter.PROPERTY);
            } else {
                System.setProperty(GameTestFilter.PROPERTY, previous);
            }
        }
    }

    private static final class SampleTests {

        @GameTest(batch = TestBatches.HEAVY)
        static void heavyByBatch(GameTestHelper h) {}

        @HeavyTest
        @GameTest
        static void heavyByAnnotation(GameTestHelper h) {}

        @GameTest
        static void normalNanoForge(GameTestHelper h) {}
    }

    @HeavyTest
    private static final class HeavyClassTests {

        @GameTest
        static void insideHeavyClass(GameTestHelper h) {}
    }
}
