package com.shiver.energycoreextend.energy;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverflowEnergyStorageTest {
    private static final BigInteger MAX = BigInteger.valueOf(Long.MAX_VALUE);

    @Test
    void transfersAcrossMultipleLongRangesWithoutLosingEnergy() {
        OverflowEnergyStorage storage = new OverflowEnergyStorage();
        assertEquals(Long.MAX_VALUE, storage.receive(Long.MAX_VALUE, false));
        assertEquals(Long.MAX_VALUE, storage.receive(Long.MAX_VALUE, true));
        assertEquals(MAX, storage.getStoredBig());
        storage.receive(Long.MAX_VALUE, false);
        assertEquals(MAX.multiply(BigInteger.valueOf(2)), storage.getStoredBig());
        assertEquals(Long.MAX_VALUE - 7, storage.extract(Long.MAX_VALUE - 7, false));
        assertEquals(MAX.add(BigInteger.valueOf(7)), storage.getStoredBig());
        assertEquals(Long.MAX_VALUE, storage.extract(Long.MAX_VALUE, false));
        assertEquals(7, storage.extract(Long.MAX_VALUE, true));
        assertEquals(BigInteger.valueOf(7), storage.getStoredBig());
        assertEquals(7, storage.extract(Long.MAX_VALUE, false));
        assertEquals(BigInteger.ZERO, storage.getStoredBig());
    }

    @Test
    void randomTransfersAndRestoresMatchBigIntegerAccounting() {
        Random random = new Random(3055);
        OverflowEnergyStorage storage = new OverflowEnergyStorage();
        BigInteger expected = BigInteger.ZERO;
        for (int i = 0; i < 4000; i++) {
            long amount = i % 3 == 0 ? Long.MAX_VALUE : random.nextLong() & Long.MAX_VALUE;
            boolean simulate = random.nextInt(4) == 0;
            if (random.nextBoolean()) {
                assertEquals(amount, storage.receive(amount, simulate));
                if (!simulate) expected = expected.add(BigInteger.valueOf(amount));
            } else {
                long extracted = expected.min(BigInteger.valueOf(amount)).longValueExact();
                assertEquals(extracted, storage.extract(amount, simulate));
                if (!simulate) expected = expected.subtract(BigInteger.valueOf(extracted));
            }
            if (i % 17 == 0) {
                OverflowEnergyStorage restored = new OverflowEnergyStorage();
                restored.restore(storage.getBase(), storage.getOverflow());
                storage = restored;
            }
            assertEquals(expected, storage.getStoredBig(), "第 " + i + " 次操作的能量不守恒");
        }
    }
}
