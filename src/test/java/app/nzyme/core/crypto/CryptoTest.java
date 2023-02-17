package app.nzyme.core.crypto;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.responses.dashboard.tls.KeyStoreUtils;
import app.nzyme.core.rest.responses.dashboard.tls.SSLEngineConfiguratorBuilder;
import com.google.common.base.Strings;
import app.nzyme.core.MockNzyme;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static org.testng.Assert.*;

public class CryptoTest {

    private static final Path FOLDER = Paths.get("crypto_test");

    @SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
    @BeforeMethod
    public void cleanDirectory() throws IOException, InterruptedException {
        Files.walk(FOLDER)
                .map(Path::toFile)
                .forEach(file -> {
                    System.out.println(file.getName());
                    // Don't delete the entire crypto_test root directory.
                    if (!file.toPath().equals(FOLDER) && !file.getName().equals(".gitkeep")) {
                        if (!file.delete()) {
                            throw new RuntimeException("Could not delete key file [" + file.getAbsolutePath() + "] to prepare tests.");
                        }
                    }
                });

        long size = Files.walk(FOLDER)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        assertEquals(size, 0, "Crypto key test folder is not empty.");
    }

    private String readKeyIdFromDB(NzymeNode nzyme) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT key_signature FROM crypto_keys " +
                        "WHERE node_id = :node_id AND key_type = 'PGP'")
                        .bind("node_id", nzyme.getNodeInformation().id())
                        .mapTo(String.class)
                        .one()
        );
    }

    @Test
    public void testInitialize() throws Crypto.CryptoInitializationException, IOException {
        NzymeNode mockNzyme = new MockNzyme();

        File privateFile = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME).toFile();
        File publicFile = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME).toFile();

        new Crypto(mockNzyme).initialize();

        assertTrue(privateFile.exists());
        assertTrue(publicFile.exists());

        assertTrue(Files.readAllBytes(privateFile.toPath()).length > 0);
        assertTrue(Files.readAllBytes(publicFile.toPath()).length > 0);
        assertFalse(Strings.isNullOrEmpty(readKeyIdFromDB(mockNzyme)));
    }

    @Test
    public void testInitializeDoesNotRegenerateKeysOnEachInit() throws Crypto.CryptoInitializationException, IOException {
        NzymeNode mockNzyme = new MockNzyme();

        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);
        Path publicPath = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME);

        new Crypto(mockNzyme).initialize();
        byte[] secret1 = Files.readAllBytes(privatePath);
        byte[] public1 = Files.readAllBytes(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        new Crypto(mockNzyme).initialize();
        byte[] secret2 = Files.readAllBytes(privatePath);
        byte[] public2 = Files.readAllBytes(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertEquals(secret1, secret2);
        assertEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfSecretMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeNode mockNzyme = new MockNzyme();

        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);
        Path publicPath = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME);

        new Crypto(mockNzyme).initialize();
        byte[] secret1 = Files.readAllBytes(privatePath);
        byte[] public1 = Files.readAllBytes(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        privatePath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        byte[] secret2 = Files.readAllBytes(privatePath);
        byte[] public2 = Files.readAllBytes(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfPublicMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeNode mockNzyme = new MockNzyme();

        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);
        Path publicPath = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME);

        new Crypto(mockNzyme).initialize();
        byte[] secret1 = Files.readAllBytes(privatePath);
        byte[] public1 = Files.readAllBytes(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        publicPath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        byte[] secret2 = Files.readAllBytes(privatePath);
        byte[] public2 = Files.readAllBytes(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfPublicAndSecretMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeNode mockNzyme = new MockNzyme();

        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);
        Path publicPath = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME);

        new Crypto(mockNzyme).initialize();
        byte[] secret1 = Files.readAllBytes(privatePath);
        byte[] public1 = Files.readAllBytes(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        privatePath.toFile().delete();
        publicPath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        byte[] secret2 = Files.readAllBytes(privatePath);
        byte[] public2 = Files.readAllBytes(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

    @Test
    public void testEncryptionDecryption() throws Crypto.CryptoInitializationException, Crypto.CryptoOperationException {
        NzymeNode mockNzyme = new MockNzyme();
        Crypto crypto = new Crypto(mockNzyme);
        crypto.initialize();

        byte[] value = "IT IS A SECRET.".getBytes();

        byte[] encrypted = crypto.encrypt(value);
        byte[] decrypted = crypto.decrypt(encrypted);

        assertEquals(decrypted, value);
    }

    @Test(expectedExceptions = { Crypto.CryptoOperationException.class }, expectedExceptionsMessageRegExp = "Cannot decrypt value.")
    public void testEncryptionDecryptionFailsWithWrongKey() throws Crypto.CryptoInitializationException, Crypto.CryptoOperationException {
        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);

        NzymeNode mockNzyme = new MockNzyme();
        Crypto crypto = new Crypto(mockNzyme);
        crypto.initialize();

        byte[] value = "IT IS A SECRET.".getBytes();
        byte[] encrypted = crypto.encrypt(value);

        privatePath.toFile().delete();
        crypto.initialize();

        crypto.decrypt(encrypted);
    }

    @Test
    public void testGenerateSelfSignedTLSCertificate() throws Crypto.CryptoOperationException, IOException {
        Crypto crypto = new Crypto(new MockNzyme());
        X509Certificate cert = crypto.generateTLSCertificate("CN=localhost.localdomain", 12);

        /*final SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator();
        final KeyStore keyStore = null; // TODO
        sslContextConfigurator.setKeyStorePass("123");
        sslContextConfigurator.setKeyStoreBytes(keystoreBytes);

        final SSLContext sslContext = sslContextConfigurator.createSSLContext(true);

        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(
                URI.create("https://localhost:22999/"),
                new ResourceConfig(),
                true,
                new SSLEngineConfigurator(sslContext, false, false, false)
        );

        httpServer.start();
        httpServer.shutdownNow();*/
    }

}