package com.shiver.energycoreextend.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferBudgetTest {
    @Test
    void intAndLongTransfersShareTheSameTick() {
        TransferBudget budget = new TransferBudget();
        budget.record(100, Integer.MAX_VALUE);
        long remaining = Long.MAX_VALUE - Integer.MAX_VALUE;
        assertEquals(remaining, budget.available(100, Long.MAX_VALUE));
        budget.record(100, remaining);
        assertEquals(0, budget.available(100, 1));
        assertEquals(Long.MAX_VALUE, budget.available(101, Long.MAX_VALUE));
        budget.record(101, Long.MAX_VALUE);
        assertEquals(0, budget.available(101, Integer.MAX_VALUE));
    }

    @Test
    void simulationsDoNotSpendOrResetTheBudget() {
        TransferBudget budget = new TransferBudget();
        budget.record(10, Long.MAX_VALUE - 7);
        assertEquals(7, budget.available(10, Long.MAX_VALUE));
        assertEquals(7, budget.available(10, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, budget.available(11, Long.MAX_VALUE));
        assertEquals(7, budget.available(10, Long.MAX_VALUE));
    }

    @Test
    void differentPortsHaveIndependentBudgets() {
        TransferBudget first = new TransferBudget();
        TransferBudget second = new TransferBudget();
        first.record(20, Long.MAX_VALUE);
        assertEquals(0, first.available(20, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, second.available(20, Long.MAX_VALUE));
    }
}
