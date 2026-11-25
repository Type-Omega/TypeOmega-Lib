package com.typeomega.lib.test;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTestFilterTest {

    @Test
    void nullOrBlankFilterIsInactiveAndMatchesEverything() {
        for (String raw : new String[] { null, "", "   ", " , , " }) {
            GameTestFilter filter = GameTestFilter.parse(raw);
            assertFalse(filter.isActive());
            assertEquals(List.of(), filter.terms());
            assertTrue(filter.matches("LibGameTests", "someTest", "defaultBatch"));
        }
    }

    @Test
    void matchesMethodClassAndBatchCaseInsensitively() {
        GameTestFilter byMethod = GameTestFilter.parse("NanoForge");
        assertTrue(byMethod.matches("MachineTests", "nanoforgeTierFour", "defaultBatch"));
        assertFalse(byMethod.matches("MachineTests", "boilerForms", "defaultBatch"));

        GameTestFilter byClass = GameTestFilter.parse("TOOLTIP");
        assertTrue(byClass.matches("TooltipGameTests", "checksLines", "defaultBatch"));
        assertFalse(byClass.matches("RecipeGameTests", "checksLines", "defaultBatch"));

        GameTestFilter byBatch = GameTestFilter.parse("Smoke");
        assertTrue(byBatch.matches("OtherTests", "runs", "smokeBatch"));
        assertFalse(byBatch.matches("OtherTests", "runs", "defaultBatch"));
    }

    @Test
    void commaSeparatedTermsMatchAnyAndIgnoreWhitespace() {
        GameTestFilter filter = GameTestFilter.parse(" nanoforge , tooltip , , ");
        assertEquals(List.of("nanoforge", "tooltip"), filter.terms());
        assertTrue(filter.matches("NanoForgeTests", "a", "defaultBatch"));
        assertTrue(filter.matches("OtherTests", "tooltipCheck", "defaultBatch"));
        assertFalse(filter.matches("BoilerTests", "boils", "defaultBatch"));
    }

    @Test
    void allTermMatchesEverything() {
        GameTestFilter filter = GameTestFilter.parse("ALL");
        assertTrue(filter.isRunAll());
        assertTrue(filter.matches("AnyClass", "anyMethod", "anyBatch"));
    }

    @Test
    void readsFromSystemProperty() {
        String previous = System.getProperty(GameTestFilter.PROPERTY);
        try {
            System.setProperty(GameTestFilter.PROPERTY, "data,module");
            GameTestFilter filter = GameTestFilter.fromSystemProperty();
            assertEquals(List.of("data", "module"), filter.terms());
            assertTrue(filter.matches("DataGameTests", "int128", "defaultBatch"));
            assertFalse(filter.matches("LibGameTests", "other", "defaultBatch"));
        } finally {
            if (previous == null) {
                System.clearProperty(GameTestFilter.PROPERTY);
            } else {
                System.setProperty(GameTestFilter.PROPERTY, previous);
            }
        }
    }
}
