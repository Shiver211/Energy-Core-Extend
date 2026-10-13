package com.shiver.energycoreextend.api;

import java.math.BigInteger;

/** 能量核心和输入输出口的完整储能、long 传输接口。 */
public interface IExtendedEnergyCore {
    BigInteger getStoredBig();

    boolean isUnlimited();

    /** 模拟时不修改储能或端口额度；返回实际可接收的数量。 */
    long receiveEnergyLong(long amount, boolean simulate);

    /** 模拟时不修改储能或端口额度；返回实际可提取的数量。 */
    long extractEnergyLong(long amount, boolean simulate);

    /** 完整的净传输速率，支持超出 Long.MAX_VALUE 的大数。 */
    default BigInteger getTransferRateBig() {
        return BigInteger.ZERO;
    }
}
