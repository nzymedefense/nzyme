package app.nzyme.core.ethernet.tls;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TlsHostnameMatcherTest {

    @Test
    public void testExactAndWildcard() {
        assertTrue(TlsHostnameMatcher.matchesName("www.example.com", "www.example.com"));
        assertTrue(TlsHostnameMatcher.matchesName("www.example.com", "WWW.example.com".toLowerCase()));
        assertTrue(TlsHostnameMatcher.matchesName("www.example.com", "*.example.com"));
        assertFalse(TlsHostnameMatcher.matchesName("example.com", "*.example.com"));
        assertFalse(TlsHostnameMatcher.matchesName("a.b.example.com", "*.example.com"));
        assertFalse(TlsHostnameMatcher.matchesName("www.example.org", "*.example.com"));
        assertFalse(TlsHostnameMatcher.matchesName("wwwexample.com", "*.example.com"));
        assertTrue(TlsHostnameMatcher.matchesName("www.example.com", "www.example.com."));
    }

    @Test
    public void testAgainstCertificate() throws Exception {
        CertificateDetails leaf = CertificateParser.parse(CertificateParserTest.leafDer());

        assertTrue(TlsHostnameMatcher.matches("tls-test.nzyme.local", leaf));
        assertTrue(TlsHostnameMatcher.matches("ALT.nzyme.local", leaf));
        assertFalse(TlsHostnameMatcher.matches("other.nzyme.local", leaf));
        assertFalse(TlsHostnameMatcher.matches("", leaf));
        assertFalse(TlsHostnameMatcher.matches(null, leaf));
    }

    @Test
    public void testChainValidation() throws Exception {
        TlsChainValidator validator = new TlsChainValidator();
        CertificateDetails leaf = CertificateParser.parse(CertificateParserTest.leafDer());
        CertificateDetails ca = CertificateParser.parse(CertificateParserTest.caDer());

        // Our test CA is not in the JVM trust store.
        TlsChainValidator.Result result = validator.validate(java.util.List.of(leaf, ca));
        assertNotNull(result.trusted());
        assertFalse(result.trusted());
        assertNotNull(result.error());

        TlsChainValidator.Result empty = validator.validate(java.util.List.of());
        assertNull(empty.trusted());
        assertNull(empty.error());
    }

    @Test
    public void testPublicChainIsTrusted() throws Exception {
        // Chain served by www.example.com, leaf first, as captured by a tap. Roots in the JVM trust store.
        java.util.List<CertificateDetails> chain = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            chain.add(CertificateParser.parse(com.google.common.io.Resources.toByteArray(
                    com.google.common.io.Resources.getResource("certificates/example_com_chain/" + i + ".der"))));
        }

        TlsChainValidator.Result result = new TlsChainValidator().validate(chain);
        assertNull(result.error(), result.error());
        assertTrue(result.trusted());

        CertificateDetails leaf = chain.get(0);
        assertTrue(TlsHostnameMatcher.matches("www.example.com", leaf));
        assertFalse(TlsHostnameMatcher.matches("www.example.org", leaf));
    }

}
