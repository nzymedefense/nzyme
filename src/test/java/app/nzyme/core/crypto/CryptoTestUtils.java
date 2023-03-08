package app.nzyme.core.crypto;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;

public class CryptoTestUtils {

    public static final Path CRYPTO_TEST_FOLDER = Paths.get("crypto_test");

    public static void cleanDB() {
        NzymeNode nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("TRUNCATE crypto_tls_certificates;")
                        .execute()
        );

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("TRUNCATE crypto_tls_certificates_wildcard;")
                        .execute()
        );

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("TRUNCATE nodes;")
                        .execute()
        );
    }

    public static void cleanFiles(Path folder) throws IOException {
        Files.walk(folder)
                .map(Path::toFile)
                .forEach(file -> {
                    // Don't delete the entire crypto_test root directory.
                    if (!file.toPath().equals(folder) && !file.getName().equals(".gitkeep")) {
                        if (!file.delete()) {
                            throw new RuntimeException("Could not delete key file [" + file.getAbsolutePath() + "] to prepare tests.");
                        }
                    }
                });

        long size = Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        assertEquals(size, 0, "Crypto key test folder is not empty.");

    }

}
