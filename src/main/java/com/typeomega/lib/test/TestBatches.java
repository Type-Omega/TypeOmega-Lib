package com.typeomega.lib.test;

import org.jetbrains.annotations.Nullable;

/** Standard batch names recognized by TypeOmega GameTest filtering. */
public final class TestBatches {

    /** Vanilla default {@code @GameTest} batch name. */
    public static final String DEFAULT = "defaultBatch";

    /** Batch for slow or heavy GameTests (excluded by default unless {@code heavy} or {@code all} is requested). */
    public static final String HEAVY = "heavy";

    private TestBatches() {}

    /** Returns whether {@code batchName} identifies the heavy batch (case-insensitive). */
    public static boolean isHeavy(@Nullable String batchName) {
        return HEAVY.equalsIgnoreCase(batchName);
    }
}
