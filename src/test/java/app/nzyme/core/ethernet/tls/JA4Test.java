package app.nzyme.core.ethernet.tls;

import app.nzyme.core.rest.resources.taps.reports.tables.tls.TlsClientHelloReport;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class JA4Test {

    // Worked example from the JA4 specification (technical_details/JA4.md).
    private static final List<Integer> SPEC_CIPHERS = List.of(
            0x1301, 0x1302, 0x1303, 0xc02b, 0xc02f, 0xc02c, 0xc030, 0xcca9, 0xcca8, 0xc013, 0xc014,
            0x009c, 0x009d, 0x002f, 0x0035
    );
    private static final List<Integer> SPEC_EXTENSIONS = List.of(
            0x0000, 0x0017, 0xff01, 0x000a, 0x000b, 0x0023, 0x0010, 0x0005, 0x000d, 0x0012, 0x0033,
            0x002d, 0x002b, 0x001b, 0x0015, 0x4469
    );
    private static final List<Integer> SPEC_SIGNATURE_ALGORITHMS = List.of(
            0x0403, 0x0804, 0x0401, 0x0503, 0x0805, 0x0501, 0x0806, 0x0601
    );

    private static TlsClientHelloReport hello(String legacyVersion,
                                              List<String> supportedVersions,
                                              String serverName,
                                              List<Integer> ciphers,
                                              List<Integer> extensions,
                                              List<Integer> signatureAlgorithms,
                                              List<String> alpn) {
        return TlsClientHelloReport.create("TLSv1.0", legacyVersion, supportedVersions, serverName, ciphers,
                extensions, List.of(), List.of(), signatureAlgorithms, alpn, false, false, false, false);
    }

    @Test
    public void testSpecificationExample() {
        TlsClientHelloReport ch = hello("TLSv1.2", List.of("TLSv1.3", "TLSv1.2"), "example.com",
                SPEC_CIPHERS, SPEC_EXTENSIONS, SPEC_SIGNATURE_ALGORITHMS, List.of("h2", "http/1.1"));

        assertEquals(JA4.calculate(ch), "t13d1516h2_8daaf6152771_e5627efa2ab1");
    }

    @Test
    public void testGreaseIsIgnored() {
        List<Integer> ciphers = new java.util.ArrayList<>(List.of(0x0a0a));
        ciphers.addAll(SPEC_CIPHERS);
        List<Integer> extensions = new java.util.ArrayList<>(List.of(0x1a1a));
        extensions.addAll(SPEC_EXTENSIONS);
        extensions.add(0xfafa);

        TlsClientHelloReport ch = hello("TLSv1.2", List.of("TLSv1.3", "TLSv1.2"), "example.com",
                ciphers, extensions, SPEC_SIGNATURE_ALGORITHMS, List.of("h2", "http/1.1"));

        assertEquals(JA4.calculate(ch), "t13d1516h2_8daaf6152771_e5627efa2ab1");
    }

    @Test
    public void testIsGrease() {
        assertTrue(JA4.isGrease(0x0a0a));
        assertTrue(JA4.isGrease(0x2a2a));
        assertTrue(JA4.isGrease(0xfafa));
        assertFalse(JA4.isGrease(0x0a1a));
        assertFalse(JA4.isGrease(0x1301));
        assertFalse(JA4.isGrease(0x0000));
    }

    @Test
    public void testLegacyVersionWithoutSupportedVersionsExtension() {
        List<Integer> extensions = List.of(0x0000, 0x0017, 0x000a, 0x000b);
        TlsClientHelloReport ch = hello("TLSv1.2", List.of(), "example.com",
                SPEC_CIPHERS, extensions, List.of(), List.of());

        assertTrue(JA4.calculate(ch).startsWith("t12d1504"));
    }

    @Test
    public void testNoSniNoAlpn() {
        List<Integer> extensions = List.of(0x0017, 0x000a, 0x000b, 0x002b);
        TlsClientHelloReport ch = hello("TLSv1.2", List.of("TLSv1.3"), null,
                SPEC_CIPHERS, extensions, List.of(), List.of());

        assertTrue(JA4.calculate(ch).startsWith("t13i150400_"));
    }

    @Test
    public void testAlpnVariants() {
        List<Integer> extensions = List.of(0x0000, 0x0010, 0x002b);

        assertTrue(JA4.calculate(hello("TLSv1.2", List.of("TLSv1.3"), "a", SPEC_CIPHERS, extensions,
                List.of(), List.of("http/1.1"))).startsWith("t13d1503h1_"));
        assertTrue(JA4.calculate(hello("TLSv1.2", List.of("TLSv1.3"), "a", SPEC_CIPHERS, extensions,
                List.of(), List.of("x"))).startsWith("t13d1503xx_"));
        assertTrue(JA4.calculate(hello("TLSv1.2", List.of("TLSv1.3"), "a", SPEC_CIPHERS, extensions,
                List.of(), List.of(" a"))).startsWith("t13d150321_"));
        assertTrue(JA4.calculate(hello("TLSv1.2", List.of("TLSv1.3"), "a", SPEC_CIPHERS, extensions,
                List.of(), List.of(""))).startsWith("t13d150300_"));
    }

    @Test
    public void testEmptyListsAndCaps() {
        TlsClientHelloReport ch = hello("TLSv1.0", List.of(), null, List.of(), List.of(), List.of(), List.of());
        assertEquals(JA4.calculate(ch), "t10i000000_000000000000_000000000000");

        List<Integer> manyCiphers = new java.util.ArrayList<>();
        for (int i = 1; i <= 150; i++) {
            manyCiphers.add(i);
        }
        assertTrue(JA4.calculate(hello("TLSv1.0", List.of(), null, manyCiphers, List.of(), List.of(), List.of()))
                .startsWith("t10i9900"));
    }

    @Test
    public void testSignatureAlgorithmsChangeExtensionHash() {
        TlsClientHelloReport with = hello("TLSv1.2", List.of("TLSv1.3"), "a", SPEC_CIPHERS, SPEC_EXTENSIONS,
                SPEC_SIGNATURE_ALGORITHMS, List.of("h2"));
        TlsClientHelloReport without = hello("TLSv1.2", List.of("TLSv1.3"), "a", SPEC_CIPHERS, SPEC_EXTENSIONS,
                List.of(), List.of("h2"));

        assertNotEquals(JA4.calculate(with), JA4.calculate(without));
        assertEquals(JA4.calculate(with).substring(0, 23), JA4.calculate(without).substring(0, 23));
    }

    @Test
    public void testNullHello() {
        assertNull(JA4.calculate(null));
    }

}
