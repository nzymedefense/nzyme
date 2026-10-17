package app.nzyme.core.ethernet.tls;

import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class CertificateParserTest {

    static byte[] leafDer() throws Exception {
        return Resources.toByteArray(Resources.getResource("certificates/tls_test_leaf.der"));
    }

    static byte[] caDer() throws Exception {
        return Resources.toByteArray(Resources.getResource("certificates/tls_test_ca.der"));
    }

    @Test
    public void testParsesLeaf() throws Exception {
        CertificateDetails leaf = CertificateParser.parse(leafDer());

        assertTrue(leaf.parsed());
        assertNull(leaf.parseError());
        // Same fingerprint the tap computes. See tap/src/wired/tls/certificate.rs testdata.
        assertEquals(BaseEncoding.base16().lowerCase().encode(leaf.sha256()),
                "60e588b0f2805d5a80f41a738035765dcc14f669afd0c6b705f955387f1e5dcb");
        assertEquals(leaf.sha1().length, 20);
        assertEquals(leaf.version(), 3);
        assertEquals(leaf.serial(), "279f8b452b92ada653fd79745e5abd10e334545e");
        assertEquals(leaf.subject(), "CN=tls-test.nzyme.local,O=nzyme test,C=US");
        assertEquals(leaf.issuer(), "CN=nzyme test CA,O=nzyme test,C=US");
        assertEquals(leaf.commonName(), "tls-test.nzyme.local");
        assertEquals(leaf.subjectAlternativeNames(),
                List.of("DNS:tls-test.nzyme.local", "DNS:alt.nzyme.local", "IP:10.0.0.1"));
        assertEquals(leaf.dnsNames(), List.of("tls-test.nzyme.local", "alt.nzyme.local"));
        assertEquals(leaf.notBefore().withZone(DateTimeZone.UTC).getYear(), 2026);
        assertEquals(leaf.notAfter().withZone(DateTimeZone.UTC).getYear(), 2126);
        assertFalse(leaf.isCa());
        assertFalse(leaf.selfSigned());
        assertEquals(leaf.publicKeyAlgorithm(), "EC");
        assertEquals(leaf.publicKeyBits(), 256);
        assertEquals(leaf.signatureAlgorithm(), "SHA256withECDSA");
        assertEquals(leaf.extendedKeyUsage(), List.of("serverAuth", "clientAuth"));
        assertNotNull(leaf.subjectKeyIdentifier());
        assertEquals(leaf.subjectKeyIdentifier().length, 20);
        assertNotNull(leaf.authorityKeyIdentifier());
        assertEquals(leaf.authorityKeyIdentifier().length, 20);
    }

    @Test
    public void testParsesCaAndLinksToLeaf() throws Exception {
        CertificateDetails ca = CertificateParser.parse(caDer());
        CertificateDetails leaf = CertificateParser.parse(leafDer());

        assertTrue(ca.parsed());
        assertTrue(ca.isCa());
        assertTrue(ca.selfSigned());
        assertEquals(ca.subject(), ca.issuer());
        assertTrue(ca.subjectAlternativeNames().isEmpty());
        assertTrue(ca.extendedKeyUsage().isEmpty());
        assertEquals(leaf.authorityKeyIdentifier(), ca.subjectKeyIdentifier());
    }

    @Test
    public void testGarbageStillYieldsFingerprints() {
        CertificateDetails garbage = CertificateParser.parse(new byte[]{1, 2, 3, 4, 5});

        assertFalse(garbage.parsed());
        assertNotNull(garbage.parseError());
        assertEquals(garbage.sha256().length, 32);
        assertEquals(garbage.sha1().length, 20);
        assertNull(garbage.subject());
        assertTrue(garbage.subjectAlternativeNames().isEmpty());
    }

}
