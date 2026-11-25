package com.typeomega.lib.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code @GameTest} method or test holder class as heavy, excluding it from default runs unless
 * {@code -Dtypeomega.tests} includes {@code heavy} or {@code all}. Equivalent to {@code batch = TestBatches.HEAVY}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface HeavyTest {}
