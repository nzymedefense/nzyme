package app.nzyme.core.crypto;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.ResourcesAccessingTest;
import app.nzyme.core.crypto.tls.*;
import app.nzyme.core.distributed.Node;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.testng.Assert.*;

public class CryptoTLSTest extends ResourcesAccessingTest {

    @BeforeMethod
    public void clean() throws IOException, InterruptedException {
        CryptoTestUtils.cleanFiles(CryptoTestUtils.CRYPTO_TEST_FOLDER);
        CryptoTestUtils.cleanDB();
    }

    @Test
    public void testLoadRSACertificateAndKeyFromFile() throws Exception {
        UUID nodeId = UUID.randomUUID();
        File cert = loadFromResourceFile("tls/rsatest.crt");
        File key = loadFromResourceFile("tls/rsatest.key");

        TLSKeyAndCertificate tls = TLSUtils.readTLSKeyAndCertificateFromInputStreams(
                nodeId,
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        assertEquals(tls.certificates().size(), 2);
        assertEquals(tls.signature(), "d15b8401c1e7dfc7acd7c1386560692883b7e9307dd353286057c200a5bc3473");
        assertEquals(tls.expiresAt().toString(), "2023-03-09T14:58:49.000-06:00");
        assertEquals(tls.validFrom().toString(), "2023-03-08T14:57:49.000-06:00");
        assertEquals(tls.nodeId(), nodeId);
        assertEquals(tls.key().getFormat(), "PKCS#8");
        assertEquals(tls.key().getAlgorithm(), "ECDSA");

        X509Certificate firstCert = tls.certificates().get(0);
        assertEquals(firstCert.getSigAlgName(), "SHA256withRSA");
        assertEquals(new DateTime(firstCert.getNotAfter()).toString(), "2023-03-09T14:58:49.000-06:00");
        assertEquals(new DateTime(firstCert.getNotBefore()).toString(), "2023-03-08T14:57:49.000-06:00");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getIssuerX500Principal().getName(), "CN=Example Intermediate CA");
        assertEquals(firstCert.getSubjectAlternativeNames(), new ArrayList<>(){{
            add(new ArrayList<>() {{
                add(2);
                add("test.nzyme.example.com");
            }});
        }});
        assertNull(firstCert.getIssuerAlternativeNames());
    }

    @Test
    public void testLoadECDSACertificateAndKeyFromFile() throws Exception {
        UUID nodeId = UUID.randomUUID();
        File cert = loadFromResourceFile("tls/ecdsatest.crt");
        File key = loadFromResourceFile("tls/ecdsatest.key");

        TLSKeyAndCertificate tls = TLSUtils.readTLSKeyAndCertificateFromInputStreams(
                nodeId,
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        assertEquals(tls.certificates().size(), 2);
        assertEquals(tls.signature(), "3aa94bfc2e0175fead5d29bacdfcbe6b968bdb13a99e37cbbfa8d7e62a37ab27");
        assertEquals(tls.expiresAt().toString(), "2023-03-09T14:51:12.000-06:00");
        assertEquals(tls.validFrom().toString(), "2023-03-08T14:50:12.000-06:00");
        assertEquals(tls.nodeId(), nodeId);
        assertEquals(tls.key().getFormat(), "PKCS#8");
        assertEquals(tls.key().getAlgorithm(), "ECDSA");

        X509Certificate firstCert = tls.certificates().get(0);
        assertEquals(firstCert.getSigAlgName(), "SHA256withECDSA");
        assertEquals(new DateTime(firstCert.getNotAfter()).toString(), "2023-03-09T14:51:12.000-06:00");
        assertEquals(new DateTime(firstCert.getNotBefore()).toString(), "2023-03-08T14:50:12.000-06:00");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getIssuerX500Principal().getName(), "CN=nzymetests Intermediate CA,O=nzymetests");
        assertEquals(firstCert.getSubjectAlternativeNames(), new ArrayList<>(){{
            add(new ArrayList<>() {{
                add(2);
                add("test.nzyme.example.com");
            }});
        }});
        assertNull(firstCert.getIssuerAlternativeNames());
    }

    @Test
    public void testLoadRSAWildcardCertificateAndKeyFromFile() throws Exception {
        File cert = loadFromResourceFile("tls/rsatest.crt");
        File key = loadFromResourceFile("tls/rsatest.key");

        TLSWildcardKeyAndCertificate tls = TLSUtils.readTLSWildcardKeyAndCertificateFromInputStreams(
                "^foo",
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        assertEquals(tls.certificates().size(), 2);
        assertEquals(tls.signature(), "d15b8401c1e7dfc7acd7c1386560692883b7e9307dd353286057c200a5bc3473");
        assertEquals(tls.expiresAt().toString(), "2023-03-09T14:58:49.000-06:00");
        assertEquals(tls.validFrom().toString(), "2023-03-08T14:57:49.000-06:00");
        assertEquals(tls.nodeMatcher(), "^foo");
        assertEquals(tls.key().getFormat(), "PKCS#8");
        assertEquals(tls.key().getAlgorithm(), "ECDSA");

        X509Certificate firstCert = tls.certificates().get(0);
        assertEquals(firstCert.getSigAlgName(), "SHA256withRSA");
        assertEquals(new DateTime(firstCert.getNotAfter()).toString(), "2023-03-09T14:58:49.000-06:00");
        assertEquals(new DateTime(firstCert.getNotBefore()).toString(), "2023-03-08T14:57:49.000-06:00");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getIssuerX500Principal().getName(), "CN=Example Intermediate CA");
        assertEquals(firstCert.getSubjectAlternativeNames(), new ArrayList<>(){{
            add(new ArrayList<>() {{
                add(2);
                add("test.nzyme.example.com");
            }});
        }});
        assertNull(firstCert.getIssuerAlternativeNames());
    }

    @Test
    public void testLoadECDSAWildcardCertificateAndKeyFromFile() throws Exception {
        File cert = loadFromResourceFile("tls/ecdsatest.crt");
        File key = loadFromResourceFile("tls/ecdsatest.key");

        TLSWildcardKeyAndCertificate tls = TLSUtils.readTLSWildcardKeyAndCertificateFromInputStreams(
                "^foo",
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        assertEquals(tls.certificates().size(), 2);
        assertEquals(tls.signature(), "3aa94bfc2e0175fead5d29bacdfcbe6b968bdb13a99e37cbbfa8d7e62a37ab27");
        assertEquals(tls.expiresAt().toString(), "2023-03-09T14:51:12.000-06:00");
        assertEquals(tls.validFrom().toString(), "2023-03-08T14:50:12.000-06:00");
        assertEquals(tls.nodeMatcher(), "^foo");
        assertEquals(tls.key().getFormat(), "PKCS#8");
        assertEquals(tls.key().getAlgorithm(), "ECDSA");

        X509Certificate firstCert = tls.certificates().get(0);
        assertEquals(firstCert.getSigAlgName(), "SHA256withECDSA");
        assertEquals(new DateTime(firstCert.getNotAfter()).toString(), "2023-03-09T14:51:12.000-06:00");
        assertEquals(new DateTime(firstCert.getNotBefore()).toString(), "2023-03-08T14:50:12.000-06:00");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getIssuerX500Principal().getName(), "CN=nzymetests Intermediate CA,O=nzymetests");
        assertEquals(firstCert.getSubjectAlternativeNames(), new ArrayList<>(){{
            add(new ArrayList<>() {{
                add(2);
                add("test.nzyme.example.com");
            }});
        }});
        assertNull(firstCert.getIssuerAlternativeNames());
    }

    @Test
    public void testLoadRSACertificateAndKeyFromDatabase() throws Exception, Crypto.CryptoInitializationException {
        MockNzyme nzyme = new MockNzyme();
        nzyme.getCrypto().initialize(false);

        File cert = loadFromResourceFile("tls/rsatest.crt");
        File key = loadFromResourceFile("tls/rsatest.key");

        TLSKeyAndCertificate tls = TLSUtils.readTLSKeyAndCertificateFromInputStreams(
                nzyme.getNodeInformation().id(),
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        nzyme.getCrypto().updateTLSCertificateOfNode(nzyme.getNodeInformation().id(), tls);

        Optional<TLSKeyAndCertificate> dbTlsR = nzyme.getCrypto().getTLSCertificateOfNode(nzyme.getNodeInformation().id());

        assertTrue(dbTlsR.isPresent());

        TLSKeyAndCertificate dbTls = dbTlsR.get();
        assertEquals(dbTls.certificates().size(), 2);
        assertEquals(dbTls.signature(), "d15b8401c1e7dfc7acd7c1386560692883b7e9307dd353286057c200a5bc3473");
        assertEquals(dbTls.expiresAt().toString(), "2023-03-09T14:58:49.000-06:00");
        assertEquals(dbTls.validFrom().toString(), "2023-03-08T14:57:49.000-06:00");
        assertEquals(dbTls.nodeId(), nzyme.getNodeInformation().id());
        assertEquals(dbTls.key().getFormat(), "PKCS#8");
        assertEquals(dbTls.key().getAlgorithm(), "EC");

        X509Certificate firstCert = dbTls.certificates().get(0);
        assertEquals(firstCert.getSigAlgName(), "SHA256withRSA");
        assertEquals(new DateTime(firstCert.getNotAfter()).toString(), "2023-03-09T14:58:49.000-06:00");
        assertEquals(new DateTime(firstCert.getNotBefore()).toString(), "2023-03-08T14:57:49.000-06:00");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getIssuerX500Principal().getName(), "CN=Example Intermediate CA");
        assertEquals(firstCert.getSubjectAlternativeNames(), new ArrayList<>(){{
            add(new ArrayList<>() {{
                add(2);
                add("test.nzyme.example.com");
            }});
        }});
        assertNull(firstCert.getIssuerAlternativeNames());
    }

    @Test
    public void testLoadECDSACertificateAndKeyFromDatabase() throws Exception, Crypto.CryptoInitializationException {
        MockNzyme nzyme = new MockNzyme();
        nzyme.getCrypto().initialize(false);

        File cert = loadFromResourceFile("tls/ecdsatest.crt");
        File key = loadFromResourceFile("tls/ecdsatest.key");

        TLSKeyAndCertificate tls = TLSUtils.readTLSKeyAndCertificateFromInputStreams(
                nzyme.getNodeInformation().id(),
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        nzyme.getCrypto().updateTLSCertificateOfNode(nzyme.getNodeInformation().id(), tls);

        Optional<TLSKeyAndCertificate> dbTlsR = nzyme.getCrypto().getTLSCertificateOfNode(nzyme.getNodeInformation().id());

        assertTrue(dbTlsR.isPresent());

        TLSKeyAndCertificate dbTls = dbTlsR.get();
        assertEquals(dbTls.certificates().size(), 2);
        assertEquals(dbTls.signature(), "3aa94bfc2e0175fead5d29bacdfcbe6b968bdb13a99e37cbbfa8d7e62a37ab27");
        assertEquals(dbTls.expiresAt().toString(), "2023-03-09T14:51:12.000-06:00");
        assertEquals(dbTls.validFrom().toString(), "2023-03-08T14:50:12.000-06:00");
        assertEquals(dbTls.nodeId(), nzyme.getNodeInformation().id());
        assertEquals(dbTls.key().getFormat(), "PKCS#8");
        assertEquals(dbTls.key().getAlgorithm(), "EC");

        X509Certificate firstCert = dbTls.certificates().get(0);
        assertEquals(firstCert.getSigAlgName(), "SHA256withECDSA");
        assertEquals(new DateTime(firstCert.getNotAfter()).toString(), "2023-03-09T14:51:12.000-06:00");
        assertEquals(new DateTime(firstCert.getNotBefore()).toString(), "2023-03-08T14:50:12.000-06:00");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getIssuerX500Principal().getName(), "CN=nzymetests Intermediate CA,O=nzymetests");
        assertEquals(firstCert.getSubjectAlternativeNames(), new ArrayList<>(){{
            add(new ArrayList<>() {{
                add(2);
                add("test.nzyme.example.com");
            }});
        }});
        assertNull(firstCert.getIssuerAlternativeNames());
    }

    @Test
    public void testLoadRSAWilcardCertificateAndKeyFromDatabase() throws Exception, Crypto.CryptoInitializationException {
        MockNzyme nzyme = new MockNzyme();
        nzyme.getCrypto().initialize(false);

        File cert = loadFromResourceFile("tls/rsatest.crt");
        File key = loadFromResourceFile("tls/rsatest.key");

        TLSWildcardKeyAndCertificate tls = TLSUtils.readTLSWildcardKeyAndCertificateFromInputStreams(
                "^foo",
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        nzyme.getCrypto().writeTLSWildcardCertificate(tls);

        TLSWildcardKeyAndCertificate dbTls = nzyme.getCrypto().getTLSWildcardCertificates().get(0);
        assertEquals(dbTls.certificates().size(), 2);
        assertEquals(dbTls.signature(), "d15b8401c1e7dfc7acd7c1386560692883b7e9307dd353286057c200a5bc3473");
        assertEquals(dbTls.expiresAt().toString(), "2023-03-09T14:58:49.000-06:00");
        assertEquals(dbTls.validFrom().toString(), "2023-03-08T14:57:49.000-06:00");
        assertEquals(dbTls.nodeMatcher(), "^foo");
        assertEquals(dbTls.key().getFormat(), "PKCS#8");
        assertEquals(dbTls.key().getAlgorithm(), "EC");

        X509Certificate firstCert = dbTls.certificates().get(0);
        assertEquals(firstCert.getSigAlgName(), "SHA256withRSA");
        assertEquals(new DateTime(firstCert.getNotAfter()).toString(), "2023-03-09T14:58:49.000-06:00");
        assertEquals(new DateTime(firstCert.getNotBefore()).toString(), "2023-03-08T14:57:49.000-06:00");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getIssuerX500Principal().getName(), "CN=Example Intermediate CA");
        assertEquals(firstCert.getSubjectAlternativeNames(), new ArrayList<>(){{
            add(new ArrayList<>() {{
                add(2);
                add("test.nzyme.example.com");
            }});
        }});
        assertNull(firstCert.getIssuerAlternativeNames());
    }

    @Test
    public void testLoadECDSAWilcardCertificateAndKeyFromDatabase() throws Exception, Crypto.CryptoInitializationException {
        MockNzyme nzyme = new MockNzyme();
        nzyme.getCrypto().initialize(false);

        File cert = loadFromResourceFile("tls/ecdsatest.crt");
        File key = loadFromResourceFile("tls/ecdsatest.key");

        TLSWildcardKeyAndCertificate tls = TLSUtils.readTLSWildcardKeyAndCertificateFromInputStreams(
                "^foo",
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        nzyme.getCrypto().writeTLSWildcardCertificate(tls);

        TLSWildcardKeyAndCertificate dbTls = nzyme.getCrypto().getTLSWildcardCertificates().get(0);
        assertEquals(dbTls.certificates().size(), 2);
        assertEquals(dbTls.signature(), "3aa94bfc2e0175fead5d29bacdfcbe6b968bdb13a99e37cbbfa8d7e62a37ab27");
        assertEquals(dbTls.expiresAt().toString(), "2023-03-09T14:51:12.000-06:00");
        assertEquals(dbTls.validFrom().toString(), "2023-03-08T14:50:12.000-06:00");
        assertEquals(dbTls.nodeMatcher(), "^foo");
        assertEquals(dbTls.key().getFormat(), "PKCS#8");
        assertEquals(dbTls.key().getAlgorithm(), "EC");

        X509Certificate firstCert = dbTls.certificates().get(0);
        assertEquals(firstCert.getSigAlgName(), "SHA256withECDSA");
        assertEquals(new DateTime(firstCert.getNotAfter()).toString(), "2023-03-09T14:51:12.000-06:00");
        assertEquals(new DateTime(firstCert.getNotBefore()).toString(), "2023-03-08T14:50:12.000-06:00");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getSubjectX500Principal().getName(), "CN=test.nzyme.example.com");
        assertEquals(firstCert.getIssuerX500Principal().getName(), "CN=nzymetests Intermediate CA,O=nzymetests");
        assertEquals(firstCert.getSubjectAlternativeNames(), new ArrayList<>(){{
            add(new ArrayList<>() {{
                add(2);
                add("test.nzyme.example.com");
            }});
        }});
    }

    @Test
    public void testLoadOrder() throws Exception, Crypto.CryptoInitializationException {
        File key = loadFromResourceFile("tls/ecdsatest.key");
        File cert = loadFromResourceFile("tls/ecdsatest.crt");

        // Create wildcard certificate if nothing else exists.
        MockNzyme nzyme = new MockNzyme();
        nzyme.getCrypto().initialize(false);
        KeyStoreBootstrapResult result = nzyme.getCrypto().bootstrapTLSKeyStore();
        assertEquals(result.loadedCertificate().sourceType(), TLSSourceType.GENERATED_SELF_SIGNED);

        // Load from disk when individual exists.
        Path keyLocation = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.TLS_KEY_FILE_NAME);
        Path certLocation = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.TLS_CERTIFICATE_FILE_NAME);
        Files.write(keyLocation, Files.readAllBytes(key.toPath()));
        Files.write(certLocation, Files.readAllBytes(cert.toPath()));
        nzyme.getCrypto().initialize(false);
        KeyStoreBootstrapResult result2 = nzyme.getCrypto().bootstrapTLSKeyStore();
        assertEquals(result2.loadedCertificate().sourceType(), TLSSourceType.FILE_LOADED);

        // Write Wildcard
        Files.delete(keyLocation);
        Files.delete(certLocation);
        TLSWildcardKeyAndCertificate wildcard = TLSUtils.readTLSWildcardKeyAndCertificateFromInputStreams(
                ".*",
                TLSSourceType.WILDCARD,
                new FileInputStream(cert),
                new FileInputStream(key)
        );
        nzyme.getNodeManager().registerSelf();
        nzyme.getCrypto().writeTLSWildcardCertificate(wildcard);
        nzyme.getCrypto().initialize(false);
        KeyStoreBootstrapResult result3 = nzyme.getCrypto().bootstrapTLSKeyStore();
        assertEquals(result3.loadedCertificate().sourceType(), TLSSourceType.WILDCARD);

        // Load from disk when wildcard exists.
        Files.write(keyLocation, Files.readAllBytes(key.toPath()));
        Files.write(certLocation, Files.readAllBytes(cert.toPath()));
        nzyme.getCrypto().initialize(false);
        KeyStoreBootstrapResult result4 = nzyme.getCrypto().bootstrapTLSKeyStore();
        assertEquals(result4.loadedCertificate().sourceType(), TLSSourceType.FILE_LOADED);
    }

    @Test
    public void testKeyStoreCreation() throws Exception, Crypto.CryptoInitializationException {
        MockNzyme nzyme = new MockNzyme();
        nzyme.getCrypto().initialize(false);

        File cert = loadFromResourceFile("tls/rsatest.crt");
        File key = loadFromResourceFile("tls/rsatest.key");

        TLSWildcardKeyAndCertificate tls = TLSUtils.readTLSWildcardKeyAndCertificateFromInputStreams(
                "^foo",
                TLSSourceType.TEST,
                new FileInputStream(cert),
                new FileInputStream(key)
        );

        nzyme.getCrypto().writeTLSWildcardCertificate(tls);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new ByteArrayInputStream(nzyme.getCrypto().bootstrapTLSKeyStore().keystoreBytes()), "".toCharArray());

        Key loadedKey = keyStore.getKey("key", "".toCharArray());
        assertEquals(loadedKey.getAlgorithm(), "RSA");
    }

    @Test
    public void testAutoGeneratesSelfSignedCertificate() throws Exception, Crypto.CryptoInitializationException {
        MockNzyme nzyme = new MockNzyme();
        assertTrue(nzyme.getCrypto().getTLSCertificateOfNode(nzyme.getNodeInformation().id()).isEmpty());
        nzyme.getCrypto().initialize(false);

        Optional<TLSKeyAndCertificate> tls = nzyme.getCrypto().getTLSCertificateOfNode(nzyme.getNodeInformation().id());
        assertTrue(tls.isPresent());
        assertEquals(tls.get().sourceType(), TLSSourceType.GENERATED_SELF_SIGNED);
    }

    @Test
    public void testWildcardNodeMatching() throws Exception, Crypto.CryptoInitializationException {
        List<Node> nodes = new ArrayList<>() {{
            add(createNodeObject("node-xx-01"));
            add(createNodeObject("node-xx-02"));
            add(createNodeObject("node-yy-01"));
            add(createNodeObject("node-zz-01"));
        }};

        TLSWildcardNodeMatcher matcher = new TLSWildcardNodeMatcher();
        assertEquals(matcher.match("^node-.*", nodes), new ArrayList<>() {{
            add(nodes.get(0));
            add(nodes.get(1));
            add(nodes.get(2));
            add(nodes.get(3));
        }});

        assertEquals(matcher.match("^node-xx-.*", nodes), new ArrayList<>() {{
            add(nodes.get(0));
            add(nodes.get(1));
        }});

        assertEquals(matcher.match(".*01$", nodes), new ArrayList<>() {{
            add(nodes.get(0));
            add(nodes.get(2));
            add(nodes.get(3));
        }});

        assertEquals(matcher.match("foo", nodes), new ArrayList<>());
    }

    private Node createNodeObject(String name) {
        return Node.create(
                UUID.randomUUID(),
                name,
                URI.create("https://127.0.0.1:22900/"),
                URI.create("https://127.0.0.1:22900/"),
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0.0D,
                0,
                DateTime.now(),
                0L,
                "foo",
                "foo",
                "0.0.0",
                DateTime.now(),
                DateTime.now(),
                0L,
                false,
                false
        );
    }

}
