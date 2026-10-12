package com.shiver.energycoreextend.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/** 不经过 double，避免大数显示丢失数量级或出现 Infinity。 */
public final class EnergyFormat {
    private static final MathContext PRECISION = new MathContext(4, RoundingMode.DOWN);
    private static final BigInteger TICKS_PER_YEAR = BigInteger.valueOf(20L * 60 * 60 * 24 * 365);

    private EnergyFormat() {}

    public static String compact(BigInteger value) {
        if (value.abs().toString().length() <= 12) return grouped(value);
        return new BigDecimal(value).round(PRECISION).toEngineeringString().replace("E+", "E");
    }

    public static String grouped(BigInteger value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    public static BigInteger dischargeTicks(BigInteger stored, long rate) {
        if (rate >= 0) return BigInteger.ZERO;
        return stored.divide(BigInteger.valueOf(rate).negate());
    }

    public static String duration(BigInteger ticks, String yearUnit, String dayUnit) {
        BigInteger[] yearsAndRemainder = ticks.divideAndRemainder(TICKS_PER_YEAR);
        long seconds = yearsAndRemainder[1].longValueExact() / 20;
        long days = seconds / 86400;
        String clock = String.format(Locale.ROOT, "%02d:%02d:%02d",
                seconds / 3600 % 24, seconds / 60 % 60, seconds % 60);
        if (yearsAndRemainder[0].signum() > 0) {
            return compact(yearsAndRemainder[0]) + " " + yearUnit + ", " + days + " " + dayUnit + " " + clock;
        }
        return days > 0 ? days + " " + dayUnit + " " + clock : clock;
    }
}
