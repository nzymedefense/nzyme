package app.nzyme.core.ethernet.recon;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class ReconExclusionMatcherTest {

    @Test
    public void testSourceCidr() {
        ReconExclusionMatcher m = ReconExclusionMatcher.create(null, "10.0.0.0/8", null, null);

        assertTrue(m.matches("TCP_PORT_SCAN", "10.1.2.3", "AA:BB:CC:DD:EE:FF", "192.168.0.1", null));
        assertFalse(m.matches("TCP_PORT_SCAN", "11.1.2.3", "AA:BB:CC:DD:EE:FF", "192.168.0.1", null));
        assertFalse(m.matches("ARP_SWEEP", null, "AA:BB:CC:DD:EE:FF", null, List.of("10.0.0.1")));
    }

    @Test
    public void testSourceSingleHost() {
        ReconExclusionMatcher m = ReconExclusionMatcher.create(null, "192.168.0.10", null, null);

        assertTrue(m.matches("TCP_HOST_SWEEP", "192.168.0.10", null, null, List.of("192.168.0.1")));
        assertFalse(m.matches("TCP_HOST_SWEEP", "192.168.0.11", null, null, List.of("192.168.0.1")));
    }

    @Test
    public void testSourceMac() {
        ReconExclusionMatcher m = ReconExclusionMatcher.create(null, null, "aa-bb-cc-dd-ee-ff", null);

        assertTrue(m.matches("ARP_SWEEP", null, "AA:BB:CC:DD:EE:FF", null, List.of("10.0.0.1")));
        assertTrue(m.matches("ARP_SWEEP", "10.0.0.5", "aa:bb:cc:dd:ee:ff", null, List.of("10.0.0.1")));
        assertFalse(m.matches("ARP_SWEEP", "10.0.0.5", "AA:BB:CC:DD:EE:00", null, List.of("10.0.0.1")));
        assertFalse(m.matches("ARP_SWEEP", "10.0.0.5", null, null, List.of("10.0.0.1")));
    }

    @Test
    public void testScanTypes() {
        ReconExclusionMatcher m = ReconExclusionMatcher.create(List.of("arp_sweep", "UDP_HOST_SWEEP"), null, null, null);

        assertTrue(m.matches("ARP_SWEEP", "10.0.0.5", null, null, List.of("10.0.0.1")));
        assertTrue(m.matches("UDP_HOST_SWEEP", "10.0.0.5", null, null, List.of("10.0.0.1")));
        assertFalse(m.matches("TCP_HOST_SWEEP", "10.0.0.5", null, null, List.of("10.0.0.1")));
        assertFalse(m.matches("BOGUS", "10.0.0.5", null, null, List.of("10.0.0.1")));
    }

    @Test
    public void testTargetCidrPortScan() {
        ReconExclusionMatcher m = ReconExclusionMatcher.create(null, null, null, "10.0.5.0/24");

        assertTrue(m.matches("TCP_PORT_SCAN", "192.168.0.1", null, "10.0.5.9", null));
        assertFalse(m.matches("TCP_PORT_SCAN", "192.168.0.1", null, "10.0.6.9", null));
    }

    @Test
    public void testTargetCidrSweep() {
        ReconExclusionMatcher m = ReconExclusionMatcher.create(null, null, null, "10.0.5.0/24");

        assertTrue(m.matches("TCP_HOST_SWEEP", "192.168.0.1", null, null, List.of("10.0.5.1", "10.0.5.200")));
        assertFalse(m.matches("TCP_HOST_SWEEP", "192.168.0.1", null, null, List.of("10.0.5.1", "10.0.6.1")));
        assertFalse(m.matches("TCP_HOST_SWEEP", "192.168.0.1", null, null, List.of()));
        assertFalse(m.matches("TCP_HOST_SWEEP", "192.168.0.1", null, null, null));
    }

    @Test
    public void testAllCriteriaMustMatch() {
        ReconExclusionMatcher m = ReconExclusionMatcher.create(List.of("TCP_PORT_SCAN"), "10.0.0.0/8", "AA:BB:CC:DD:EE:FF", "10.0.5.0/24");

        assertTrue(m.matches("TCP_PORT_SCAN", "10.1.1.1", "AA:BB:CC:DD:EE:FF", "10.0.5.1", null));
        assertFalse(m.matches("UDP_PORT_SCAN", "10.1.1.1", "AA:BB:CC:DD:EE:FF", "10.0.5.1", null));
        assertFalse(m.matches("TCP_PORT_SCAN", "11.1.1.1", "AA:BB:CC:DD:EE:FF", "10.0.5.1", null));
        assertFalse(m.matches("TCP_PORT_SCAN", "10.1.1.1", "AA:BB:CC:DD:EE:00", "10.0.5.1", null));
        assertFalse(m.matches("TCP_PORT_SCAN", "10.1.1.1", "AA:BB:CC:DD:EE:FF", "10.0.6.1", null));
    }

    @Test
    public void testNormalization() {
        assertEquals(ReconExclusionMatcher.normalizeMac(" aa-bb-cc-dd-ee-ff "), "AA:BB:CC:DD:EE:FF");
        assertEquals(ReconExclusionMatcher.normalizeCidr("10.0.0.0/8"), "10.0.0.0/8");
        assertEquals(ReconExclusionMatcher.normalizeCidr("10.0.0.5/32"), "10.0.0.5");
        assertNull(ReconExclusionMatcher.normalizeCidr("  "));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsEmptyCriteria() {
        ReconExclusionMatcher.create(null, null, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsInvalidCidr() {
        ReconExclusionMatcher.create(null, "10.0.0.0/33", null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsHostBitsSet() {
        ReconExclusionMatcher.create(null, null, null, "10.0.5.1/24");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsInvalidMac() {
        ReconExclusionMatcher.create(null, null, "not-a-mac", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsUnknownType() {
        ReconExclusionMatcher.create(List.of("PING_SWEEP"), null, null, null);
    }

}
