package app.nzyme.core.security.authentication;

import com.codahale.metrics.MetricRegistry;
import com.google.common.io.BaseEncoding;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.testng.Assert.*;

public class PasswordHasherTest {

    @Test
    public void testHashesAreNotReproducible() {
        PasswordHasher hasher = new PasswordHasher(new MetricRegistry());

        String password = "eOJF8DV00Fhy8oSbpc9Ebf";

        PasswordHasher.GeneratedHashAndSalt hash1 = hasher.createHash(password);
        PasswordHasher.GeneratedHashAndSalt hash2 = hasher.createHash(password);

        assertNotEquals(hash1.salt(), hash2.salt());
        assertNotEquals(hash1.hash(), hash2.hash());
    }

    @Test
    public void testHashSucceedsWithCorrectPasswordAndHash() {
        PasswordHasher hasher = new PasswordHasher(new MetricRegistry());

        String password = "eOJF8DV00Fhy8oSbpc9Ebf";

        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(password);

        assertEquals(hash.hash().length(), 344);
        assertNotEquals(hash.hash(), BaseEncoding.base64().encode(password.getBytes(StandardCharsets.UTF_8)));

        assertTrue(hasher.compareHash(password, hash.hash(), hash.salt()));
        assertFalse(hasher.compareHash("w39743UB4O61atVDdjhlW7253", hash.hash(), hash.salt()));
    }

    @Test
    public void testHashFailsWithWrongPassword() {
        PasswordHasher hasher = new PasswordHasher(new MetricRegistry());

        String password = "eOJF8DV00Fhy8oSbpc9Ebf";

        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(password);

        assertEquals(hash.hash().length(), 344);
        assertNotEquals(hash.hash(), BaseEncoding.base64().encode(password.getBytes(StandardCharsets.UTF_8)));

        assertFalse(hasher.compareHash(password + "123", hash.hash(), hash.salt()));
    }

    @Test
    public void testHashFailsWithWrongSalt() {
        PasswordHasher hasher = new PasswordHasher(new MetricRegistry());

        String password = "eOJF8DV00Fhy8oSbpc9Ebf";

        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(password);

        assertEquals(hash.hash().length(), 344);
        assertNotEquals(hash.hash(), BaseEncoding.base64().encode(password.getBytes(StandardCharsets.UTF_8)));

        byte[] wrongSalt = new byte[128];
        new SecureRandom().nextBytes(wrongSalt);

        assertFalse(hasher.compareHash(password, hash.hash(), BaseEncoding.base64().encode(wrongSalt)));
    }

    @Test
    public void testHashFailsWithWrongSaltWrongPassword() {
        PasswordHasher hasher = new PasswordHasher(new MetricRegistry());

        String password = "eOJF8DV00Fhy8oSbpc9Ebf";

        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(password);

        assertEquals(hash.hash().length(), 344);
        assertNotEquals(hash.hash(), BaseEncoding.base64().encode(password.getBytes(StandardCharsets.UTF_8)));

        byte[] wrongSalt = new byte[128];
        new SecureRandom().nextBytes(wrongSalt);

        assertFalse(hasher.compareHash(password + "123", hash.hash(), BaseEncoding.base64().encode(wrongSalt)));
    }

    @Test
    public void testHashingSucceedsWithLongPassword() {
        String password128 = "0B33kfUhXMPYpqYyfXL3V4194oj7Y0J7d0YVGa76P4L15JRXTOB40ydccaCXZDynwJnRcUUyOq8Yw1k7sGfblBvcgRmL5cGobiOaj8If2F1WzSafs3VzoFDgLDRYMyvN";
        new PasswordHasher(new MetricRegistry()).createHash(password128);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*maximum length is 128 characters.*")
    public void testHashingFailsWithTooLongPassword() {
        String password129 = "UW0R8rzgZq1xHkWPqXTYn8TqKm1654AWnxzHXPa1B8bKreqh3KTDqZgVy5oTN7WX2w23R1utMCYkwTFrM5UakiTC4nCv6sviJKcmSYItGXIloDEPDLsaMkoxDoTiS2Mc3";
        new PasswordHasher(new MetricRegistry()).createHash(password129);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*minimum length is 12 characters.*")
    public void testHashingFailsWithTooShortPassword() {
        String password11 = "sqc0lFM3eHL";
        new PasswordHasher(new MetricRegistry()).createHash(password11);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*NULL or empty password provided.*")
    public void testHashingFailsWithNullPassword() {
        new PasswordHasher(new MetricRegistry()).createHash(null);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Password maximum length is 128 characters.*")
    public void testComparisonFailsWithTooLongPassword() {
        String password129 = "UW0R8rzgZq1xHkWPqXTYn8TqKm1654AWnxzHXPa1B8bKreqh3KTDqZgVy5oTN7WX2w23R1utMCYkwTFrM5UakiTC4nCv6sviJKcmSYItGXIloDEPDLsaMkoxDoTiS2Mc3";
        new PasswordHasher(new MetricRegistry()).compareHash(password129, "bar", "baz");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*minimum length is 12 characters.*")
    public void testComparisonFailsWithTooShortPassword() {
        String password11 = "sqc0lFM3eHL";
        new PasswordHasher(new MetricRegistry()).compareHash(password11, "bar", "baz");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*NULL or empty password provided.*")
    public void testComparisonFailsWithNullPassword() {
        new PasswordHasher(new MetricRegistry()).compareHash(null, "bar", "baz");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Incorrect salt length: 256.*")
    public void testComparisonFailsWithWrongLengthOfSalt() {
        byte[] salt = new byte[256];
        new SecureRandom().nextBytes(salt);

        new PasswordHasher(new MetricRegistry()).compareHash("eOJF8DV00Fhy8oSbpc9Ebf", "bar", BaseEncoding.base64().encode(salt));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*NULL or empty salt provided.*")
    public void testComparisonFailsWithNullSalt() {
        new PasswordHasher(new MetricRegistry()).compareHash("eOJF8DV00Fhy8oSbpc9Ebf", "bar", null);
    }

}