package com.dnagda.eliteG.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Constants class
 */
public class ConstantsTest {

    @Test
    public void testConstants_AreNotNull() {
        assertNotNull("ADB grant command should not be null", Constants.ADB_GRANT_COMMAND);
        assertNotNull("GitHub setup URL should not be null", Constants.GITHUB_SETUP_URL);
        assertNotNull("Package name should not be null", Constants.PACKAGE_NAME);
    }

    @Test
    public void testConstants_HaveValidValues() {
        assertTrue("Max recent games should be positive", Constants.MAX_RECENT_GAMES > 0);
        assertTrue("Default resolution scale should be valid", 
                   Constants.DEFAULT_RESOLUTION_SCALE >= 50 && Constants.DEFAULT_RESOLUTION_SCALE <= 100);
        assertTrue("Min resolution scale should be valid", 
                   Constants.MIN_RESOLUTION_SCALE >= 50 && Constants.MIN_RESOLUTION_SCALE <= 100);
        assertTrue("Max resolution scale should be valid", 
                   Constants.MAX_RESOLUTION_SCALE >= 50 && Constants.MAX_RESOLUTION_SCALE <= 100);
    }

    @Test
    public void testConstants_ResolutionScaleRange() {
        assertTrue("Min resolution should be less than or equal to default", 
                   Constants.MIN_RESOLUTION_SCALE <= Constants.DEFAULT_RESOLUTION_SCALE);
        assertTrue("Default resolution should be less than or equal to max", 
                   Constants.DEFAULT_RESOLUTION_SCALE <= Constants.MAX_RESOLUTION_SCALE);
    }

    @Test
    public void testConstants_TimeoutValues() {
        assertTrue("ADB timeout should be positive", Constants.ADB_TIMEOUT_MS > 0);
        assertTrue("UI timeout should be positive", Constants.UI_TIMEOUT_MS > 0);
        assertTrue("Performance check timeout should be positive", Constants.PERFORMANCE_CHECK_TIMEOUT_MS > 0);
    }

    @Test
    public void testConstants_MemoryThresholds() {
        assertTrue("Low memory threshold should be positive", Constants.LOW_MEMORY_THRESHOLD_MB > 0);
        assertTrue("High memory threshold should be positive", Constants.HIGH_MEMORY_THRESHOLD_MB > 0);
        assertTrue("Low memory threshold should be less than high memory threshold", 
                   Constants.LOW_MEMORY_THRESHOLD_MB < Constants.HIGH_MEMORY_THRESHOLD_MB);
    }
}