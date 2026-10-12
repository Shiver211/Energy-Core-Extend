package com.shiver.energycoreextend.api;

import java.math.BigInteger;

/** 能量核心的完整储能视图，不受 RF / FE 查询接口的整数范围限制。 */
public interface IExtendedEnergyCore {
    BigInteger getStoredBig();

    boolean isUnlimited();
}
