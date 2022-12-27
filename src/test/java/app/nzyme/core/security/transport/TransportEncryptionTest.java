package app.nzyme.core.security.transport;

import org.testng.annotations.Test;

import java.security.GeneralSecurityException;

import static org.testng.Assert.*;

public class TransportEncryptionTest {

    @Test
    public void testEncrypt() throws Exception {
        String key = "aigeimoiR3fohgh7eicaijahChai8zi1";
        String message = "hello there. this is an encrypted letter.";

        byte[] encrypted = new TransportEncryption(key).encrypt(message.getBytes());
        byte[] decrypted = new TransportEncryption(key).decrypt(encrypted);

        assertNotEquals(encrypted, message.getBytes());
        assertNotEquals(new String(encrypted), message);

        assertEquals(message, new String(decrypted));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testThrowsExceptionForTooShortKey() throws GeneralSecurityException {
        new TransportEncryption("tooshort");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testThrowsExceptionForTooLongKey() throws GeneralSecurityException {
        new TransportEncryption("Oophahhahxo2aeS4Eic4iu7ka2Haa0vahquekohjeGhahy0dee9aPheo4chooweesoo3wooTahKieWai9kohqu8ou7");
    }

}