package app.nzyme.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class PidFile {

    private static final Logger LOG = LogManager.getLogger(PidFile.class);

    public static boolean isAlreadyRunning(String nodeName) throws IOException {
        Path pidFile = getPidFile(nodeName);

        try {
            // Attempt atomic claim of the PID file.
            Files.writeString(pidFile, String.valueOf(ProcessHandle.current().pid()), StandardOpenOption.CREATE_NEW);
            return false;
        } catch (FileAlreadyExistsException e) {
            // File exists, check if the owning process is still alive.
            try {
                String existing = Files.readString(pidFile).trim();
                long existingPid = Long.parseLong(existing);

                Optional<ProcessHandle> handle = ProcessHandle.of(existingPid);
                if (handle.isPresent() && handle.get().isAlive()) {
                    return true; // Genuinely already running.
                }

                // Stale PID file — delete and reclaim.
                Files.delete(pidFile);
                Files.writeString(pidFile, String.valueOf(ProcessHandle.current().pid()), StandardOpenOption.CREATE_NEW);
                return false;
            } catch (NumberFormatException ex) {
                // Corrupted PID file. Do not start.
                LOG.error("Corrupted PID file at [{}]", pidFile.toAbsolutePath(), ex);
                return true;
            }
        }
    }

    private static Path getPidFile(String nodeName) {
        if (!nodeName.matches("^[a-zA-Z0-9_-]{1,64}$")) {
            throw new IllegalArgumentException("Invalid node name: " + nodeName);
        }
        return Path.of(System.getProperty("java.io.tmpdir"), nodeName + ".pid");
    }

}