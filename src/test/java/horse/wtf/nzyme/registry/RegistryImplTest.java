package horse.wtf.nzyme.registry;

import app.nzyme.plugin.Registry;
import app.nzyme.plugin.RegistryCryptoException;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.crypto.Crypto;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.testng.Assert.*;


public class RegistryImplTest {

    private static final Path FOLDER = Paths.get("crypto_test");

    @BeforeMethod
    public void cleanDatabase() {
        new MockNzyme().getDatabase().useHandle(handle -> {
            handle.createUpdate("DELETE FROM registry")
                    .execute();
            handle.createUpdate("DELETE FROM registry_encrypted")
                    .execute();
        });
    }

    @Test
    public void testValue() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");

        assertTrue(r.getValue("foo").isEmpty());
        assertNull(r.getValueOrNull("foo"));

        r.setValue("foo", "bar");
        assertEquals(r.getValue("foo").get(), "bar");
        assertEquals(r.getValueOrNull("foo"), "bar");
    }

    @Test
    public void testValueEncrypted() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(nzyme, "test");

        assertTrue(r.getEncryptedValue("foo").isEmpty());
        assertNull(r.getEncryptedValueOrNull("foo"));

        r.setEncryptedValue("foo", "bar");
        assertEquals(r.getEncryptedValue("foo").get(), "bar");
        assertEquals(r.getEncryptedValueOrNull("foo"), "bar");
    }

    @Test(expectedExceptions = {RegistryCryptoException.class},
            expectedExceptionsMessageRegExp = "Could not decrypt registry value for key \\[foo\\]")
    public void testValueEncryptedFailsAfterKeyChange() throws RegistryCryptoException, Crypto.CryptoInitializationException {
        Path privatePath = Paths.get(FOLDER.toString(), Crypto.PGP_PRIVATE_KEY_NAME);

        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(nzyme, "test");

        assertTrue(r.getEncryptedValue("foo").isEmpty());
        assertNull(r.getEncryptedValueOrNull("foo"));

        r.setEncryptedValue("foo", "bar");

        privatePath.toFile().delete();
        nzyme.getCrypto().initialize();

        r.getEncryptedValue("foo");
    }

    @Test
    public void testUpdatedValue() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");

        assertTrue(r.getValue("foo").isEmpty());
        assertNull(r.getValueOrNull("foo"));

        r.setValue("foo", "bar");
        assertEquals(r.getValue("foo").get(), "bar");
        assertEquals(r.getValueOrNull("foo"), "bar");

        r.setValue("foo", "bar2");
        assertEquals(r.getValue("foo").get(), "bar2");
        assertEquals(r.getValueOrNull("foo"), "bar2");
    }

    @Test
    public void testUpdatedEncryptedValue() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");

        assertTrue(r.getEncryptedValue("foo").isEmpty());
        assertNull(r.getEncryptedValueOrNull("foo"));

        r.setEncryptedValue("foo", "bar");
        assertEquals(r.getEncryptedValue("foo").get(), "bar");
        assertEquals(r.getEncryptedValueOrNull("foo"), "bar");

        r.setEncryptedValue("foo", "bar2");
        assertEquals(r.getEncryptedValue("foo").get(), "bar2");
        assertEquals(r.getEncryptedValueOrNull("foo"), "bar2");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry key\\.")
    public void testSetNullKey() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setValue(null, "foo");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry key\\.")
    public void testSetEncryptedNullKey() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setEncryptedValue(null, "foo");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry key\\.")
    public void testSetEmptyKey() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setValue("", "foo");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry key\\.")
    public void testSetEncryptedEmptyKey() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setEncryptedValue("", "foo");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry key\\.")
    public void testSetEmptyKeyTrim() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setValue(" ", "foo");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry key\\.")
    public void testSetEncryptedEmptyKeyTrim() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setEncryptedValue(" ", "foo");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry value for key \\[test\\.foo\\]\\.")
    public void testSetNullValue() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setValue("foo", null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry value for key \\[test\\.foo\\]\\.")
    public void testSetEncryptedNullValue() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setEncryptedValue("foo", null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry value for key \\[test\\.foo\\]\\.")
    public void testSetEmptyValue() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setValue("foo", "");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry value for key \\[test\\.foo\\]\\.")
    public void testSetEncryptedEmptyValue() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setEncryptedValue("foo", "");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry value for key \\[test\\.foo\\]\\.")
    public void testSetEmptyValueTrim() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setValue("foo", " ");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Empty or null registry value for key \\[test\\.foo\\]\\.")
    public void testSetEncryptedEmptyValueTrim() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setEncryptedValue("foo", " ");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Key length exceeded\\.")
    public void testSetExceedinglyLongKey() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setValue(randomString(129), "bar");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Key length exceeded\\.")
    public void testSetEncryptedExceedinglyLongKey() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setEncryptedValue(randomString(129), "bar");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Value length exceeded\\.")
    public void testSetExceedinglyLongValue() {
        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setValue("foo", randomString(256));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Value length exceeded\\.")
    public void testSetEncryptedExceedinglyLongValue() throws RegistryCryptoException {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.initialize();

        Registry r = new RegistryImpl(new MockNzyme(), "test");
        r.setEncryptedValue("foo", randomString(256));
    }

    private String randomString(int length) {
        Random random = new Random();

        return random.ints(48, 122 + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}