package app.nzyme.core.distributed;

import app.nzyme.core.MockNzyme;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.testng.Assert.*;

public class NodeManagerTest {

    // TODO read data dir from mock configuration

    @BeforeMethod
    public void cleanDataDirectory() throws IOException {
        Files.walk(DATA_DIR)
                .map(Path::toFile)
                .forEach(file -> {
                    // Don't delete the entire crypto_test root directory.
                    if (!file.toPath().equals(DATA_DIR)) {
                        if (!file.delete()) {
                            throw new RuntimeException("Could not delete test data file [" + file.getAbsolutePath() + "] to prepare tests.");
                        }
                    }
                });

        long size = Files.walk(DATA_DIR)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        assertEquals(size, 0, "Test data folder is not empty.");
    }

    @Test
    public void testBuildsAndReadsNodeId() throws NodeManager.NodeInitializationException {
        Path nodeIdFile = Path.of(DATA_DIR.toString(), "node_id");
        assertFalse(Files.exists(nodeIdFile));

        NodeManager nm = new NodeManager(new MockNzyme());
        nm.initialize();

        assertTrue(Files.exists(nodeIdFile));

        UUID nodeId1 = nm.getNodeId();
        assertNotNull(nodeId1);

        nm.initialize();
        UUID nodeId2 = nm.getNodeId();
        assertEquals(nodeId1, nodeId2);
    }

    @Test
    public void testBuildsReadsAndRebuildsNodeId() throws NodeManager.NodeInitializationException, IOException {
        Path nodeIdFile = Path.of(DATA_DIR.toString(), "node_id");
        assertFalse(Files.exists(nodeIdFile));

        NodeManager nm = new NodeManager(new MockNzyme());
        nm.initialize();

        assertTrue(Files.exists(nodeIdFile));

        UUID nodeId1 = nm.getNodeId();
        assertNotNull(nodeId1);

        nm.initialize();
        UUID nodeId2 = nm.getNodeId();
        assertEquals(nodeId1, nodeId2);

        Files.delete(nodeIdFile);
        assertFalse(Files.exists(nodeIdFile));

        nm.initialize();
        UUID nodeId3 = nm.getNodeId();
        assertNotNull(nodeId3);
        assertNotEquals(nodeId1, nodeId3);
    }

}