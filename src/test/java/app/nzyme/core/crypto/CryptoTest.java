package app.nzyme.core.crypto;

import com.google.common.base.Strings;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeLeader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

public class CryptoTest {

    private static final Path FOLDER = Paths.get("crypto_test");

    @SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
    @BeforeMethod
    public void cleanDirectory() throws IOException, InterruptedException {
        Files.walk(FOLDER)
                .map(Path::toFile)
                .forEach(file -> {
                    // Don't delete the entire crypto_test root directory.
                    if (!file.toPath().equals(FOLDER)) {
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

    private String readKeyIdFromDB(NzymeLeader nzyme) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT key_signature FROM crypto_keys " +
                        "WHERE node = :node AND key_type = 'PGP'")
                        .bind("node", nzyme.getNodeID())
                        .mapTo(String.class)
                        .one()
        );
    }

    @Test
    public void testInitialize() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        File privateFile = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME).toFile();
        File publicFile = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME).toFile();

        new Crypto(mockNzyme).initialize();

        assertTrue(privateFile.exists());
        assertTrue(publicFile.exists());

        assertTrue(Files.readString(privateFile.toPath()).startsWith("-----BEGIN PGP PRIVATE KEY BLOCK-----"));
        assertTrue(Files.readString(publicFile.toPath()).startsWith("-----BEGIN PGP PUBLIC KEY BLOCK-----"));
        assertFalse(Strings.isNullOrEmpty(readKeyIdFromDB(mockNzyme)));
    }

    @Test
    public void testInitializeDoesNotRegenerateKeysOnEachInit() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);
        Path publicPath = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME);

        new Crypto(mockNzyme).initialize();
        String secret1 = Files.readString(privatePath);
        String public1 = Files.readString(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        new Crypto(mockNzyme).initialize();
        String secret2 = Files.readString(privatePath);
        String public2 = Files.readString(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertEquals(secret1, secret2);
        assertEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfSecretMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);
        Path publicPath = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME);

        new Crypto(mockNzyme).initialize();
        String secret1 = Files.readString(privatePath);
        String public1 = Files.readString(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        privatePath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        String secret2 = Files.readString(privatePath);
        String public2 = Files.readString(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfPublicMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);
        Path publicPath = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME);

        new Crypto(mockNzyme).initialize();
        String secret1 = Files.readString(privatePath);
        String public1 = Files.readString(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        publicPath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        String secret2 = Files.readString(privatePath);
        String public2 = Files.readString(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfPublicAndSecretMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);
        Path publicPath = Paths.get(FOLDER.toString(), Crypto.PGP_PUBLIC_KEY_NAME);

        new Crypto(mockNzyme).initialize();
        String secret1 = Files.readString(privatePath);
        String public1 = Files.readString(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        privatePath.toFile().delete();
        publicPath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        String secret2 = Files.readString(privatePath);
        String public2 = Files.readString(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

    @Test
    public void testEncryptionDecryption() throws Crypto.CryptoInitializationException, Crypto.CryptoOperationException {
        NzymeLeader mockNzyme = new MockNzyme();
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

        NzymeLeader mockNzyme = new MockNzyme();
        Crypto crypto = new Crypto(mockNzyme);
        crypto.initialize();

        byte[] value = "IT IS A SECRET.".getBytes();
        byte[] encrypted = crypto.encrypt(value);

        privatePath.toFile().delete();
        crypto.initialize();

        crypto.decrypt(encrypted);
    }

}