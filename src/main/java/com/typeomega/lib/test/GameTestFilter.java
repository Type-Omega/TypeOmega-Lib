package com.typeomega.lib.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filters registered GameTests using {@code -Dtypeomega.tests=<terms>}, matching comma-separated terms
 * (case-insensitive substring match) against method name, class simple name or batch name.
 * Tests in {@link TestBatches#HEAVY} (or annotated with {@link HeavyTest}) are excluded by default unless
 * {@code heavy} or {@code all} is present.
 */
public final class GameTestFilter {

    /** System property holding the comma-separated list of GameTest filter terms (or {@code all}). */
    public static final String PROPERTY = "typeomega.tests";

    /** Term that explicitly enables every GameTest, including the heavy batch. */
    public static final String ALL = "all";

    private static final Logger LOGGER = LogManager.getLogger("typeomegalib");
    private static final AtomicInteger REGISTERED_COUNT = new AtomicInteger();
    private static final AtomicInteger SKIPPED_COUNT = new AtomicInteger();
    private static final AtomicInteger SKIPPED_HEAVY_COUNT = new AtomicInteger();
    private static final AtomicBoolean HOOKED_SERVER_LOG = new AtomicBoolean();

    private final String raw;
    private final List<String> terms;
    private final boolean runAll;
    private final boolean heavyAllowed;

    private GameTestFilter(String raw, List<String> terms) {
        this.raw = raw;
        this.terms = terms;
        this.runAll = terms.contains(ALL);
        this.heavyAllowed = this.runAll || terms.contains(TestBatches.HEAVY);
    }

    /** Reads the active filter from {@link #PROPERTY}. */
    public static GameTestFilter fromSystemProperty() {
        return parse(System.getProperty(PROPERTY));
    }

    /** Parses a comma-separated filter string into a {@link GameTestFilter}. */
    public static GameTestFilter parse(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return new GameTestFilter("", List.of());
        }
        List<String> parsed = new ArrayList<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim().toLowerCase(Locale.ROOT);
            if (!trimmed.isEmpty()) {
                parsed.add(trimmed);
            }
        }
        return new GameTestFilter(raw.trim(), List.copyOf(parsed));
    }

    public boolean isActive() {
        return !terms.isEmpty();
    }

    public boolean isRunAll() {
        return runAll;
    }

    public boolean isHeavyAllowed() {
        return heavyAllowed;
    }

    public List<String> terms() {
        return terms;
    }

    /** Returns whether a GameTest with the given class simple name, method/test name and batch passes this filter. */
    public boolean matches(@Nullable String className, @Nullable String methodName, @Nullable String batchName) {
        return matches(className, methodName, batchName, false);
    }

    /**
     * Returns whether a GameTest passes this filter, treating {@code markedHeavy} or {@link TestBatches#HEAVY}
     * as heavy.
     */
    public boolean matches(@Nullable String className, @Nullable String methodName, @Nullable String batchName,
                           boolean markedHeavy) {
        boolean heavy = markedHeavy || TestBatches.isHeavy(batchName);
        // EN: Heavy tests stay out of default and targeted runs unless 'heavy' or 'all' is requested.
        // PT: Testes pesados ficam de fora por padrão e em filtros comuns até que 'heavy' ou 'all' seja pedido.
        // ZH: 重型测试在默认及常规过滤运行中会被排除，除非显式指定 'heavy' 或 'all'。
        if (heavy && !heavyAllowed) {
            return false;
        }
        if (!isActive() || runAll) {
            return true;
        }
        return matchesAnyTerm(className) || matchesAnyTerm(methodName) || matchesAnyTerm(batchName) ||
                (markedHeavy && matchesAnyTerm(TestBatches.HEAVY));
    }

    /** Returns whether an annotated {@link GameTest} method passes this filter. */
    public boolean matchesMethod(Method testMethod) {
        GameTest annotation = testMethod.getAnnotation(GameTest.class);
        String batch = annotation == null ? "" : annotation.batch();
        return matches(testMethod.getDeclaringClass().getSimpleName(), testMethod.getName(), batch,
                isAnnotatedHeavy(testMethod));
    }

    /** Returns whether a {@link TestFunction} produced by a {@code @GameTestGenerator} method passes this filter. */
    public boolean matchesGenerated(Method generatorMethod, TestFunction function) {
        String className = generatorMethod.getDeclaringClass().getSimpleName();
        String batch = function.getBatchName();
        boolean markedHeavy = isAnnotatedHeavy(generatorMethod);
        return matches(className, function.getTestName(), batch, markedHeavy) ||
                matches(className, generatorMethod.getName(), batch, markedHeavy);
    }

    /** Returns whether {@code testMethod} is marked heavy via {@link TestBatches#HEAVY} or {@link HeavyTest}. */
    public static boolean isHeavy(Method testMethod) {
        if (isAnnotatedHeavy(testMethod)) {
            return true;
        }
        GameTest annotation = testMethod.getAnnotation(GameTest.class);
        return annotation != null && TestBatches.isHeavy(annotation.batch());
    }

    /** Returns whether a generated {@link TestFunction} is heavy via its method or its own batch name. */
    public static boolean isHeavy(Method generatorMethod, TestFunction function) {
        return isAnnotatedHeavy(generatorMethod) || TestBatches.isHeavy(function.getBatchName());
    }

    private static boolean isAnnotatedHeavy(Method testMethod) {
        return testMethod.isAnnotationPresent(HeavyTest.class) ||
                testMethod.getDeclaringClass().isAnnotationPresent(HeavyTest.class);
    }

    private boolean matchesAnyTerm(@Nullable String candidate) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }
        String lower = candidate.toLowerCase(Locale.ROOT);
        for (String term : terms) {
            if (lower.contains(term)) {
                return true;
            }
        }
        return false;
    }

    public static void recordRegistered(int count) {
        if (count > 0) {
            ensureServerLogHook();
            REGISTERED_COUNT.addAndGet(count);
        }
    }

    public static void recordSkipped(boolean heavy) {
        ensureServerLogHook();
        SKIPPED_COUNT.incrementAndGet();
        if (heavy) {
            SKIPPED_HEAVY_COUNT.incrementAndGet();
        }
    }

    public static int registeredCount() {
        return REGISTERED_COUNT.get();
    }

    public static int skippedCount() {
        return SKIPPED_COUNT.get();
    }

    public static int skippedHeavyCount() {
        return SKIPPED_HEAVY_COUNT.get();
    }

    private static void ensureServerLogHook() {
        if (HOOKED_SERVER_LOG.compareAndSet(false, true)) {
            MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent e) -> logSummary(LOGGER));
        }
    }

    /** Logs a one-line summary of the GameTest filter if any GameTests were processed. */
    public static void logSummary(Logger logger) {
        int registered = REGISTERED_COUNT.get();
        int skipped = SKIPPED_COUNT.get();
        int skippedHeavy = SKIPPED_HEAVY_COUNT.get();
        if (registered == 0 && skipped == 0) {
            return;
        }
        GameTestFilter active = fromSystemProperty();
        String heavyNote = skippedHeavy > 0 ? " (" + skippedHeavy + " heavy excluded)" : "";
        if (active.isActive()) {
            logger.info("GameTest filter ({}='{}'): {} registered, {} skipped{}", PROPERTY, active.raw, registered,
                    skipped, heavyNote);
        } else {
            logger.info("GameTest filter: {} registered, {} skipped{}", registered, skipped, heavyNote);
        }
        if (registered == 0) {
            logger.warn("No GameTests matched filter {}='{}'", PROPERTY, active.raw);
        }
    }
}
