package com.shiver.energycoreextend.energy;

import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.common.core.FluxUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineChartLimitTest {

    private long computeMaxUnitY(List<Long> data) {
        long max = 0L;
        if (data != null) {
            for (Long v : data) {
                if (v != null && v > max) {
                    max = v;
                }
            }
        }
        if (max <= 0L) {
            return 1L;
        }
        int log10 = (int) Math.log10(max);
        switch (log10) {
            case 0:
                return max + 1L;
            case 1:
                return (max / 5L + 1L) * 5L;
            case 2:
                return (max / 50L + 1L) * 50L;
            default:
                if (log10 >= 18) {
                    long unit = 100_000_000_000_000_000L;
                    long div = max / unit;
                    if (div >= 92) {
                        return Long.MAX_VALUE;
                    } else {
                        return (div + 1L) * unit;
                    }
                } else {
                    long unit = (long) Math.pow(10.0, log10 - 1);
                    return (max / unit + 1L) * unit;
                }
        }
    }

    @Test
    void testLongMaxDoesNotOverflow() {
        long maxUnit = computeMaxUnitY(Collections.singletonList(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, maxUnit);
        assertTrue(maxUnit > 0);

        String formatted = FluxUtils.format(maxUnit, FluxUtils.TypeNumberFormat.COMPACT, "");
        assertEquals("9.2E", formatted);
    }

    @Test
    void testVariousLargeValuesDoNotOverflow() {
        long[] testValues = {
            1_000_000_000_000_000_000L, // 1E18
            5_000_000_000_000_000_000L, // 5E18
            9_000_000_000_000_000_000L, // 9E18
            9_223_372_036_854_775_807L  // Long.MAX_VALUE
        };
        for (long v : testValues) {
            long maxUnit = computeMaxUnitY(Collections.singletonList(v));
            assertTrue(maxUnit > 0, "maxUnit must be positive for " + v);
            assertTrue(maxUnit >= v, "maxUnit must be >= value");
            String formatted = FluxUtils.format(maxUnit, FluxUtils.TypeNumberFormat.COMPACT, "");
            assertFalse(formatted.startsWith("-"), "formatted must not be negative");
            assertTrue(formatted.endsWith("E"), "formatted must end with E");
        }
    }
}

