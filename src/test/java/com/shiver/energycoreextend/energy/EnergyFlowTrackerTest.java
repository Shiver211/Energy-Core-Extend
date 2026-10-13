package com.shiver.energycoreextend.energy;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyFlowTrackerTest {
    private static final BigInteger MAX = BigInteger.valueOf(Long.MAX_VALUE);

    @Test
    void sustainedLongMaxTransferIsAveragedBeforeClamping() {
        EnergyFlowTracker tracker = new EnergyFlowTracker();
        tracker.reset(BigInteger.ZERO);
        for (int i = 1; i <= 40; i++) {
            long rate = tracker.sample(MAX.multiply(BigInteger.valueOf(i)));
            long expected = MAX.multiply(BigInteger.valueOf(Math.min(i, 20)))
                    .divide(BigInteger.valueOf(20)).longValueExact();
            assertEquals(expected, rate);
        }
    }

    @Test
    void multiplePortsAndDischargeKeepTheCorrectSign() {
        EnergyFlowTracker tracker = new EnergyFlowTracker();
        BigInteger energy = MAX.multiply(BigInteger.valueOf(100));
        tracker.reset(energy);
        for (int i = 0; i < 20; i++) {
            energy = energy.subtract(MAX.multiply(BigInteger.valueOf(2)));
            tracker.sample(energy);
        }
        assertEquals(Long.MIN_VALUE, tracker.sample(energy.subtract(MAX.multiply(BigInteger.valueOf(2)))));
        tracker.reset(energy);
        assertEquals(0, tracker.sample(energy));
    }
}
