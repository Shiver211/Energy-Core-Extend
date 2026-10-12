package com.shiver.energycoreextend.energy;

import java.math.BigInteger;

/** 总能量 = base + overflow * Long.MAX_VALUE。每次传输仍是一个 long。 */
public final class OverflowEnergyStorage {
    public static final BigInteger BASE = BigInteger.valueOf(Long.MAX_VALUE);

    private long base;
    private BigInteger overflow = BigInteger.ZERO;

    public long receive(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        if (!simulate) {
            long remaining = Long.MAX_VALUE - base;
            if (amount > remaining) {
                base = amount - remaining;
                overflow = overflow.add(BigInteger.ONE);
            } else {
                base += amount;
            }
        }
        return amount;
    }

    public long extract(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        long extracted = overflow.signum() == 0 ? Math.min(base, amount) : amount;
        if (!simulate) {
            if (extracted > base) {
                // 借位时必须保留差额，不能直接把基础值重置为 Long.MAX_VALUE。
                base = Long.MAX_VALUE - (extracted - base);
                overflow = overflow.subtract(BigInteger.ONE);
            } else {
                base -= extracted;
            }
        }
        return extracted;
    }

    public BigInteger getStoredBig() {
        return BigInteger.valueOf(base).add(overflow.multiply(BASE));
    }

    public long getStoredLimited(long limit) {
        return overflow.signum() > 0 ? limit : Math.min(base, limit);
    }

    public void setStoredBig(BigInteger stored) {
        if (stored.signum() < 0) throw new IllegalArgumentException("储能不能为负数");
        BigInteger[] parts = stored.divideAndRemainder(BASE);
        restore(parts[1].longValueExact(), parts[0]);
    }

    public void restore(long base, BigInteger overflow) {
        if (base < 0 || overflow.signum() < 0) {
            throw new IllegalArgumentException("储能的基础值和溢出次数不能为负数");
        }
        this.base = base;
        this.overflow = overflow;
    }

    public long getBase() {
        return base;
    }

    public BigInteger getOverflow() {
        return overflow;
    }
}
