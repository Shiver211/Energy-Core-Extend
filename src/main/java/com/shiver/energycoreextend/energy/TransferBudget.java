package com.shiver.energycoreextend.energy;

/** 每个输入输出口的所有方向和接口共享同一 tick 的额度。 */
public final class TransferBudget {
    private long tick = Long.MIN_VALUE;
    private long used;

    public long available(long currentTick, long requested) {
        long remaining = currentTick == tick ? Long.MAX_VALUE - used : Long.MAX_VALUE;
        return Math.min(Math.max(0, requested), remaining);
    }

    /** 只记录已经完成、且不超过 available 返回值的传输。 */
    public void record(long currentTick, long transferred) {
        if (tick != currentTick) {
            tick = currentTick;
            used = 0;
        }
        used += transferred;
    }
}
