package com.arenapvp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionComparatorTest {

    @Test
    void detectsNewerVersion() {
        assertTrue(VersionComparator.isNewerAvailable("1.0.0", "1.1.0"));
        assertTrue(VersionComparator.isNewerAvailable("v1.0.0", "1.0.1"));
        assertFalse(VersionComparator.isNewerAvailable("2.0.0", "1.9.9"));
        assertFalse(VersionComparator.isNewerAvailable("1.0.0", "1.0.0"));
    }
}
