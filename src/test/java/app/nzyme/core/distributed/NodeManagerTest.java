package app.nzyme.core.distributed;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.testng.Assert.*;

public class NodeManagerTest {

    @BeforeMethod
    public void cleanDataDirectory() throws IOException {
        Path dataDir = Path.of("test_data_dir");

        Files.walk(dataDir)
                .map(Path::toFile)
                .forEach(file -> {
                    // Don't delete the entire crypto_test root directory.
                    if (!file.toPath().equals(dataDir)) {
                        if (!file.delete()) {
                            throw new RuntimeException("Could not delete test data file [" + file.getAbsolutePath() + "] to prepare tests.");
                        }
                    }
                });

        long size = Files.walk(dataDir)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        assertEquals(size, 0, "Test data folder is not empty.");
    }

    @Test
    public void testBuildsAndReadsNodeId() throws NodeManager.NodeInitializationException {
        Path dataDir = Path.of("test_data_dir");

        Path nodeIdFile = Path.of(dataDir.toString(), "node_id");
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
        Path dataDir = Path.of("test_data_dir");

        Path nodeIdFile = Path.of(dataDir.toString(), "node_id");
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