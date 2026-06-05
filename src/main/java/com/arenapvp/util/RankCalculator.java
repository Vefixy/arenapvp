package com.arenapvp.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RankCalculator {

    private RankCalculator() {
    }

    public record RankTier(String material, int level) {
        public String displayName() {
            String name = material.substring(0, 1).toUpperCase(Locale.ROOT) + material.substring(1);
            return name + " " + level;
        }
    }

    public static RankTier calculate(int kills, Map<String, List<Integer>> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            return new RankTier("iron", 1);
        }

        List<TierEntry> entries = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> tier : tiers.entrySet()) {
            List<Integer> thresholds = tier.getValue();
            for (int i = 0; i < thresholds.size(); i++) {
                entries.add(new TierEntry(tier.getKey(), i + 1, thresholds.get(i)));
            }
        }
        entries.sort(Comparator.comparingInt(TierEntry::threshold).reversed());

        for (TierEntry entry : entries) {
            if (kills >= entry.threshold()) {
                return new RankTier(entry.material(), entry.level());
            }
        }
        return new RankTier(entries.get(entries.size() - 1).material(), 1);
    }

    public static double kdr(int kills, int deaths) {
        if (deaths == 0) {
            return kills;
        }
        return Math.round((kills / (double) deaths) * 100.0) / 100.0;
    }

    public static Map<String, List<Integer>> defaultTiers() {
        Map<String, List<Integer>> tiers = new LinkedHashMap<>();
        tiers.put("iron", List.of(10, 25, 50));
        tiers.put("gold", List.of(75, 100, 150));
        tiers.put("diamond", List.of(200, 275, 350));
        tiers.put("netherite", List.of(500, 650, 800));
        return tiers;
    }

    private record TierEntry(String material, int level, int threshold) {
    }
}
