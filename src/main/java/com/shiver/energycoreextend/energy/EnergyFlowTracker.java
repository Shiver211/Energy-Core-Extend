package com.shiver.energycoreextend.energy;

import java.math.BigInteger;
import java.util.Arrays;

/** 与原界面一致，统计最近 20 tick 的平均净流量。 */
public final class EnergyFlowTracker {
    private final BigInteger[] samples = new BigInteger[20];
    private BigInteger previous;
    private BigInteger total = BigInteger.ZERO;
    private int next;

    public void reset(BigInteger stored) {
        previous = stored;
        total = BigInteger.ZERO;
        next = 0;
        Arrays.fill(samples, BigInteger.ZERO);
    }

    public BigInteger sampleBig(BigInteger stored) {
        if (previous == null) reset(stored);
        BigInteger delta = stored.subtract(previous);
        total = total.subtract(samples[next]).add(delta);
        samples[next] = delta;
        previous = stored;
        next = (next + 1) % samples.length;
        return total.divide(BigInteger.valueOf(samples.length));
    }

    public long sample(BigInteger stored) {
        return sampleBig(stored).max(BigInteger.valueOf(Long.MIN_VALUE))
                .min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }
}
