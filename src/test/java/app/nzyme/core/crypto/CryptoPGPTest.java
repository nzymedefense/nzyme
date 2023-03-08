package app.nzyme.core.crypto;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.tls.TLSKeyAndCertificate;
import com.google.common.base.Strings;
import app.nzyme.core.MockNzyme;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

public class CryptoPGPTest {

    @SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
    @BeforeMethod
    public void clean() throws IOException, InterruptedException {
        CryptoTestUtils.cleanFiles(CryptoTestUtils.CRYPTO_TEST_FOLDER);
        CryptoTestUtils.cleanDB();
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

        File privateFile = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_FILE_NAME).toFile();
        File publicFile = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_FILE_NAME).toFile();

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

        Path privatePath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_FILE_NAME);
        Path publicPath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_FILE_NAME);

        new Crypto(mockNzyme).initialize(false);
        byte[] secret1 = Files.readAllBytes(privatePath);
        byte[] public1 = Files.readAllBytes(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        new Crypto(mockNzyme).initialize(false);
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

        Path privatePath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_FILE_NAME);
        Path publicPath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_FILE_NAME);

        new Crypto(mockNzyme).initialize();
        byte[] secret1 = Files.readAllBytes(privatePath);
        byte[] public1 = Files.readAllBytes(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        CryptoTestUtils.cleanDB();
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

        Path privatePath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_FILE_NAME);
        Path publicPath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_FILE_NAME);

        new Crypto(mockNzyme).initialize();
        byte[] secret1 = Files.readAllBytes(privatePath);
        byte[] public1 = Files.readAllBytes(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        CryptoTestUtils.cleanDB();
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

        Path privatePath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_FILE_NAME);
        Path publicPath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_FILE_NAME);

        new Crypto(mockNzyme).initialize();
        byte[] secret1 = Files.readAllBytes(privatePath);
        byte[] public1 = Files.readAllBytes(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        CryptoTestUtils.cleanDB();
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
        Path privatePath = Paths.get(CryptoTestUtils.CRYPTO_TEST_FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_FILE_NAME);

        NzymeNode mockNzyme = new MockNzyme();
        Crypto crypto = new Crypto(mockNzyme);
        crypto.initialize();

        byte[] value = "IT IS A SECRET.".getBytes();
        byte[] encrypted = crypto.encrypt(value);

        crypto.initialize();
        privatePath.toFile().delete();

        crypto.decrypt(encrypted);
    }

    @Test
    public void testGenerateSelfSignedTLSCertificate() throws Crypto.CryptoOperationException, IOException, InterruptedException {
        Crypto crypto = new Crypto(new MockNzyme());
        TLSKeyAndCertificate tlsData = crypto.generateTLSCertificate("CN=localhost.localdomain", 12);
    }



}