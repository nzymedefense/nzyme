package app.nzyme.core.ethernet.beacons;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class BeaconExclusionMatcherTest {

    @Test
    public void testDestinationCidr() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create("10.0.0.0/8", null, null, null, null, null);

        assertTrue(m.matches("TCP", null, "10.1.2.3", 80, "192.168.0.1", null));
        assertFalse(m.matches("TCP", null, "11.1.2.3", 80, "192.168.0.1", null));
        assertFalse(m.matches("DNS", "example.com", null, null, "192.168.0.1", null));
        assertFalse(m.matches("TCP", null, "2001:db8::1", 80, "192.168.0.1", null));
    }

    @Test
    public void testDestinationSingleHost() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create("95.216.195.133", null, null, null, null, null);

        assertTrue(m.matches("HTTP", null, "95.216.195.133", 80, "192.168.0.1", null));
        assertFalse(m.matches("HTTP", null, "95.216.195.134", 80, "192.168.0.1", null));
    }

    @Test
    public void testIpv6Cidr() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create("2001:db8::/32", null, null, null, null, null);

        assertTrue(m.matches("TCP", null, "2001:db8:1::1", 443, "fe80::1", null));
        assertFalse(m.matches("TCP", null, "2001:db9::1", 443, "fe80::1", null));
    }

    @Test
    public void testHostWildcard() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create(null, "*.mozilla.com", null, null, null, null);

        assertTrue(m.matches("TLS", "push.services.mozilla.com", "1.2.3.4", 443, "192.168.0.1", null));
        assertTrue(m.matches("TLS", "Token.Services.Mozilla.com.", "1.2.3.4", 443, "192.168.0.1", null));
        assertTrue(m.matches("TLS", "mozilla.com", "1.2.3.4", 443, "192.168.0.1", null));
        assertFalse(m.matches("TLS", "mozilla.org", "1.2.3.4", 443, "192.168.0.1", null));
        assertFalse(m.matches("TLS", "notmozilla.com", "1.2.3.4", 443, "192.168.0.1", null));
        assertFalse(m.matches("HTTP", null, "1.2.3.4", 80, "192.168.0.1", null));
    }

    @Test
    public void testHostExact() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create(null, "mtalk.google.com", null, null, null, null);

        assertTrue(m.matches("TLS", "mtalk.google.com", null, 5228, "192.168.0.1", null));
        assertFalse(m.matches("TLS", "alt.mtalk.google.com", null, 5228, "192.168.0.1", null));
    }

    @Test
    public void testHostInfixWildcard() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create(null, "*.services.*", null, null, null, null);

        assertTrue(m.matches("TLS", "push.services.mozilla.com", null, 443, "192.168.0.1", null));
        assertFalse(m.matches("TLS", "services.mozilla.com", null, 443, "192.168.0.1", null));
    }

    @Test
    public void testPortsAndRanges() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create(null, null, "80, 443, 8000-8100", null, null, null);

        assertTrue(m.matches("TCP", null, "1.2.3.4", 80, "192.168.0.1", null));
        assertTrue(m.matches("TCP", null, "1.2.3.4", 443, "192.168.0.1", null));
        assertTrue(m.matches("TCP", null, "1.2.3.4", 8050, "192.168.0.1", null));
        assertFalse(m.matches("TCP", null, "1.2.3.4", 8101, "192.168.0.1", null));
        assertFalse(m.matches("DNS", "example.com", null, null, "192.168.0.1", null));

        assertEquals(BeaconExclusionMatcher.normalizePorts(" 80 ,443,8000-8100,"), "80,443,8000-8100");
    }

    @Test
    public void testTypes() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create(null, null, null, List.of("DNS", "tls"), null, null);

        assertTrue(m.matches("DNS", "example.com", null, null, "192.168.0.1", null));
        assertTrue(m.matches("TLS", "example.com", "1.2.3.4", 443, "192.168.0.1", null));
        assertFalse(m.matches("HTTP", null, "1.2.3.4", 80, "192.168.0.1", null));
    }

    @Test
    public void testSourceCriteria() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create(null, null, null, null, "192.168.0.0/24", "74:56:3c:43:77:af");

        assertTrue(m.hasSourceCriteria());
        assertTrue(m.matches("TLS", "example.com", "1.2.3.4", 443, "192.168.0.133", "74:56:3C:43:77:AF"));
        assertFalse(m.matches("TLS", "example.com", "1.2.3.4", 443, "192.168.1.133", "74:56:3C:43:77:AF"));
        assertFalse(m.matches("TLS", "example.com", "1.2.3.4", 443, "192.168.0.133", "00:00:00:00:00:01"));
        assertFalse(m.matches("TLS", "example.com", "1.2.3.4", 443, "192.168.0.133", null));
    }

    @Test
    public void testCombinedCriteriaAreAnded() {
        BeaconExclusionMatcher m = BeaconExclusionMatcher.create("10.0.0.0/8", null, "443", List.of("TLS"), "192.168.0.0/16", null);

        assertFalse(m.hasSourceCriteria() == false);
        assertTrue(m.matches("TLS", "internal.example", "10.5.5.5", 443, "192.168.0.1", null));
        assertFalse(m.matches("TLS", "internal.example", "10.5.5.5", 8443, "192.168.0.1", null));
        assertFalse(m.matches("HTTP", "internal.example", "10.5.5.5", 443, "192.168.0.1", null));
        assertFalse(m.matches("TLS", "internal.example", "10.5.5.5", 443, "172.16.0.1", null));
    }

    @Test
    public void testNormalization() {
        assertEquals(BeaconExclusionMatcher.normalizeCidr(" 10.0.0.0/8 "), "10.0.0.0/8");
        assertEquals(BeaconExclusionMatcher.normalizeCidr("1.2.3.4/32"), "1.2.3.4");
        assertEquals(BeaconExclusionMatcher.normalizeHostPattern(" *.Example.COM. "), "*.example.com");
        assertEquals(BeaconExclusionMatcher.normalizeMac("74-56-3c-43-77-af"), "74:56:3C:43:77:AF");
        assertNull(BeaconExclusionMatcher.normalizeCidr(""));
        assertNull(BeaconExclusionMatcher.normalizeHostPattern(null));
        assertNull(BeaconExclusionMatcher.normalizePorts("  "));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsNoCriteria() {
        BeaconExclusionMatcher.create(null, "", " ", List.of(), null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsHostBitsInCidr() {
        BeaconExclusionMatcher.create("10.1.2.3/8", null, null, null, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsInvalidAddress() {
        BeaconExclusionMatcher.create("example.com", null, null, null, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsMatchAllHost() {
        BeaconExclusionMatcher.create(null, "*", null, null, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsInvalidPortRange() {
        BeaconExclusionMatcher.create(null, null, "9000-8000", null, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsPortOutOfRange() {
        BeaconExclusionMatcher.create(null, null, "70000", null, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsUnknownType() {
        BeaconExclusionMatcher.create(null, null, null, List.of("FTP"), null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectsInvalidMac() {
        BeaconExclusionMatcher.create(null, null, null, null, null, "74:56:3C:43:77");
    }

}
