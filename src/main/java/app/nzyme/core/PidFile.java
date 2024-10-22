package app.nzyme.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class PidFile {

    private static final Logger LOG = LogManager.getLogger(PidFile.class);

    private final static String PID_FILE_NAME = "nzyme-node.pid";

    public static boolean isAlreadyRunning() throws IOException {
        Path pidFile = getPidFile();
        if (pidFile.toFile().exists()) {
            // Read the PID from the file.
            String pidString = new String(Files.readAllBytes(pidFile), StandardCharsets.UTF_8).trim();

            // Check if the process with this PID is running.
            Optional<ProcessHandle> processHandle = ProcessHandle.of(Long.parseLong(pidString));
            if (processHandle.isPresent() && processHandle.get().isAlive()) {
                // A process with this PID is running, meaning nzyme is already running.
                return true;
            }
        }

        // Create or overwrite the PID file with the current process ID.
        Files.writeString(pidFile, String.valueOf(ProcessHandle.current().pid()));
        return false;
    }

    public static void removePidFile() {
        Path pidFile = getPidFile();
        try {
            Files.deleteIfExists(pidFile);
        } catch (IOException e) {
            LOG.error("Could not delete PID file at [{}]", pidFile);
        }
    }

    private static Path getPidFile() {
        return Path.of(System.getProperty("java.io.tmpdir"), PID_FILE_NAME);
    }

}
