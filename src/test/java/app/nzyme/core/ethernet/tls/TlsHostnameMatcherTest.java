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

}
