package com.arenapvp.util;

public final class TopPlaceholderParser {

    public enum TopField {
        NAME, VALUE, KILLS, UNKNOWN
    }

    public record TopRequest(TopCategory category, int position, TopField field) {}

    public enum TopCategory {
        KILLS, DEATHS, STREAK
    }

    private TopPlaceholderParser() {
    }

    public static TopRequest parse(String params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        String lower = params.toLowerCase();
        TopCategory category;
        String remainder;
        if (lower.startsWith("top_kills_")) {
            category = TopCategory.KILLS;
            remainder = lower.substring("top_kills_".length());
        } else if (lower.startsWith("top_deaths_")) {
            category = TopCategory.DEATHS;
            remainder = lower.substring("top_deaths_".length());
        } else if (lower.startsWith("top_streak_")) {
            category = TopCategory.STREAK;
            remainder = lower.substring("top_streak_".length());
        } else {
            return null;
        }

        int underscore = remainder.indexOf('_');
        if (underscore <= 0) {
            return null;
        }
        try {
            int position = Integer.parseInt(remainder.substring(0, underscore));
            String fieldName = remainder.substring(underscore + 1);
            TopField field = switch (fieldName) {
                case "name" -> TopField.NAME;
                case "val", "value" -> TopField.VALUE;
                case "kills" -> TopField.KILLS;
                default -> TopField.UNKNOWN;
            };
            if (field == TopField.UNKNOWN) {
                return null;
            }
            return new TopRequest(category, position, field);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
