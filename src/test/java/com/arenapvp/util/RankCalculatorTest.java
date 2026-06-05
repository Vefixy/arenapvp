package com.arenapvp.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RankCalculatorTest {

    @Test
    void calculatesIronRanks() {
        Map<String, List<Integer>> tiers = RankCalculator.defaultTiers();
        assertEquals("Iron 1", RankCalculator.calculate(10, tiers).displayName());
        assertEquals("Iron 2", RankCalculator.calculate(25, tiers).displayName());
        assertEquals("Iron 3", RankCalculator.calculate(50, tiers).displayName());
    }

    @Test
    void calculatesHighTiers() {
        Map<String, List<Integer>> tiers = RankCalculator.defaultTiers();
        assertEquals("Gold 1", RankCalculator.calculate(75, tiers).displayName());
        assertEquals("Diamond 2", RankCalculator.calculate(275, tiers).displayName());
        assertEquals("Netherite 3", RankCalculator.calculate(800, tiers).displayName());
    }

    @Test
    void defaultsToLowestTier() {
        Map<String, List<Integer>> tiers = RankCalculator.defaultTiers();
        assertEquals("Iron 1", RankCalculator.calculate(0, tiers).displayName());
    }

    @Test
    void kdrHandlesZeroDeaths() {
        assertEquals(5.0, RankCalculator.kdr(5, 0));
        assertEquals(2.5, RankCalculator.kdr(5, 2));
    }
}
