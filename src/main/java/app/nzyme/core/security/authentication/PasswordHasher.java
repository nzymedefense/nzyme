package app.nzyme.core.security.authentication;

import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.auto.value.AutoValue;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class PasswordHasher {

    private final Timer hashingTimer;

    public PasswordHasher(MetricRegistry metrics) {
        this.hashingTimer = metrics.timer(MetricNames.PASSWORD_HASHING_TIMER);
    }

    public GeneratedHashAndSalt createHash(String password) {
        runPasswordPreconditions(password);

        // Generate 128 bit salt.
        byte[] salt = new byte[128];
        new SecureRandom().nextBytes(salt);

        byte[] hash = generateHash(password, salt);

        return GeneratedHashAndSalt.create(
                BaseEncoding.base64().encode(hash),
                BaseEncoding.base64().encode(salt)
        );
    }

    private void runPasswordPreconditions(String password) {
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("NULL or empty password provided.");
        }

        if (password.length() > 128) {
            throw new RuntimeException("Password maximum length is 128 characters.");
        }

        if (password.length() < 12) {
            throw new RuntimeException("Password minimum length is 12 characters.");
        }
    }

    public boolean compareHash(String password, String hash, String salt) {
        runPasswordPreconditions(password);

        if (salt == null || salt.isEmpty()) {
            throw new RuntimeException("NULL or empty salt provided.");
        }

        byte[] saltBytes = BaseEncoding.base64().decode(salt);

        if (saltBytes.length != 128) {
            throw new RuntimeException("Incorrect salt length: " + saltBytes.length);
        }

        byte[] compareHash = generateHash(password, saltBytes);

        return Arrays.equals(compareHash, BaseEncoding.base64().decode(hash));
    }

    private byte[] generateHash(String password, byte[] salt) {
        Timer.Context timer = hashingTimer.time();
        Argon2BytesGenerator generator = new Argon2BytesGenerator();

        // We pick very expensive parameters here because there will not be a lot of parallel hashing operations in nzyme.
        int iterations = 20;
        int memLimitKilobyte = 47104; // 46 MB
        int outputLength = 256;
        int parallelism = 1;

        generator.init(
                new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                        .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                        .withIterations(iterations)
                        .withMemoryAsKB(memLimitKilobyte)
                        .withParallelism(parallelism)
                        .withSalt(salt)
                        .build()
        );

        byte[] hash = new byte[outputLength];
        generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), hash, 0, hash.length);
        timer.stop();

        return hash;
    }

    @AutoValue
    public static abstract class GeneratedHashAndSalt {

        public abstract String hash();
        public abstract String salt();

        public static GeneratedHashAndSalt create(String hash, String salt) {
            return builder()
                    .hash(hash)
                    .salt(salt)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_PasswordHasher_GeneratedHashAndSalt.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder hash(String hash);

            public abstract Builder salt(String salt);

            public abstract GeneratedHashAndSalt build();
        }

    }

}
