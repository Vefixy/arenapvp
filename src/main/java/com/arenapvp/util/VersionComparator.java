package com.arenapvp.util;

public final class VersionComparator {

    private VersionComparator() {
    }

    public static int compare(String current, String latest) {
        String a = normalize(current);
        String b = normalize(latest);
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");
        int length = Math.max(aParts.length, bParts.length);
        for (int i = 0; i < length; i++) {
            int aVal = i < aParts.length ? parsePart(aParts[i]) : 0;
            int bVal = i < bParts.length ? parsePart(bParts[i]) : 0;
            if (aVal != bVal) {
                return Integer.compare(aVal, bVal);
            }
        }
        return 0;
    }

    public static boolean isNewerAvailable(String current, String latest) {
        return compare(current, latest) < 0;
    }

    private static String normalize(String version) {
        if (version == null) {
            return "0";
        }
        return version.trim().replaceFirst("^[vV]", "");
    }

    private static int parsePart(String part) {
        String digits = part.replaceAll("[^0-9].*$", "").replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }
}
