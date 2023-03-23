package app.nzyme.core.distributed;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import com.google.common.base.Strings;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.testng.Assert.*;

public class NodeManagerTest {

    @BeforeMethod
    public void cleanData() throws IOException {
        // Always make sure to run this first. Code below has to delete node_id file created by this MockNzyme.
        NzymeNode nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("TRUNCATE nodes;")
                        .execute()
        );

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("TRUNCATE registry;")
                        .execute()
        );

        Path dataDir = Path.of("test_data_dir");

        Files.walk(dataDir)
                .map(Path::toFile)
                .forEach(file -> {
                    // Don't delete the entire crypto_test root directory.
                    if (!file.toPath().equals(dataDir) && !file.getName().equals(".gitkeep")) {
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

        UUID nodeId1 = nm.getLocalNodeId();
        assertNotNull(nodeId1);

        nm.initialize();
        UUID nodeId2 = nm.getLocalNodeId();
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

        UUID nodeId1 = nm.getLocalNodeId();
        assertNotNull(nodeId1);

        nm.initialize();
        UUID nodeId2 = nm.getLocalNodeId();
        assertEquals(nodeId1, nodeId2);

        Files.delete(nodeIdFile);
        assertFalse(Files.exists(nodeIdFile));

        nm.initialize();
        UUID nodeId3 = nm.getLocalNodeId();
        assertNotNull(nodeId3);
        assertNotEquals(nodeId1, nodeId3);
    }

    @Test
    public void testRegistersSelf() throws NodeManager.NodeInitializationException {
        NzymeNode nzyme = new MockNzyme();
        NodeManager nm = new NodeManager(nzyme);
        assertTrue(nm.getNodes().isEmpty());

        nm.initialize();

        assertTrue(nm.getNodes().isEmpty());

        nm.registerSelf();

        assertEquals(nm.getNodes().size(), 1);
        Node node = nm.getNodes().get(0);

        assertEquals(node.uuid(), nm.getLocalNodeId());
        assertEquals(node.name(), "mocky-mock");
        assertEquals(node.version(), nzyme.getVersion().getVersion().toString());
        assertEquals(node.httpExternalUri(), URI.create("https://127.0.0.1:23900/"));
        assertTrue(node.lastSeen().isBefore(DateTime.now()));
        assertTrue(node.lastSeen().isAfter(DateTime.now().minusMinutes(1)));

        assertTrue(node.memoryBytesTotal() > 0);
        assertTrue(node.memoryBytesAvailable() > 0);
        assertTrue(node.memoryBytesUsed() > 0);
        assertTrue(node.heapBytesTotal() > 0);
        assertTrue(node.heapBytesAvailable() > 0);
        assertTrue(node.heapBytesUsed() > 0);
        assertTrue(node.cpuSystemLoad() > 0);
        assertTrue(node.cpuThreadCount() > 0);
        assertTrue(node.processStartTime().isBefore(DateTime.now()));
        assertTrue(node.processVirtualSize() > 0);
        assertFalse(Strings.isNullOrEmpty(node.processArguments()));
        assertFalse(Strings.isNullOrEmpty(node.osInformation()));
    }

    @Test
    public void testUpdatesSelf() throws NodeManager.NodeInitializationException, InterruptedException {
        NzymeNode nzyme = new MockNzyme();
        NodeManager nm = new NodeManager(nzyme);
        assertTrue(nm.getNodes().isEmpty());

        nm.initialize();

        assertTrue(nm.getNodes().isEmpty());

        nm.registerSelf();

        assertEquals(nm.getNodes().size(), 1);
        Node node = nm.getNodes().get(0);

        UUID firstUUID = node.uuid();
        String firstName = node.name();
        URI firstHttpExternalUri = node.httpExternalUri();
        String firstVersion = node.version();
        DateTime firstTs = node.lastSeen();
        assertEquals(firstUUID, nm.getLocalNodeId());
        assertEquals(firstName, "mocky-mock");
        assertEquals(firstVersion, nzyme.getVersion().getVersion().toString());
        assertEquals(firstHttpExternalUri, URI.create("https://127.0.0.1:23900/"));
        assertTrue(firstTs.isBefore(DateTime.now()));
        assertTrue(firstTs.isAfter(DateTime.now().minusMinutes(1)));

        assertTrue(node.memoryBytesTotal() > 0);
        assertTrue(node.memoryBytesAvailable() > 0);
        assertTrue(node.memoryBytesUsed() > 0);
        assertTrue(node.heapBytesTotal() > 0);
        assertTrue(node.heapBytesAvailable() > 0);
        assertTrue(node.heapBytesUsed() > 0);
        assertTrue(node.cpuSystemLoad() > 0);
        assertTrue(node.cpuThreadCount() > 0);
        assertTrue(node.processStartTime().isBefore(DateTime.now()));
        assertTrue(node.processVirtualSize() > 0);
        assertFalse(Strings.isNullOrEmpty(node.processArguments()));
        assertFalse(Strings.isNullOrEmpty(node.osInformation()));

        Thread.sleep(1000);

        nm.registerSelf();

        assertEquals(nm.getNodes().size(), 1);
        node = nm.getNodes().get(0);
        assertEquals(node.uuid(), firstUUID);
        assertEquals(node.name(), firstName);
        assertEquals(node.version(), firstVersion);
        assertEquals(node.httpExternalUri(), firstHttpExternalUri);
        assertNotEquals(node.lastSeen(), firstTs);

        assertTrue(node.memoryBytesTotal() > 0);
        assertTrue(node.memoryBytesAvailable() > 0);
        assertTrue(node.memoryBytesUsed() > 0);
        assertTrue(node.cpuSystemLoad() > 0);
        assertTrue(node.cpuThreadCount() > 0);
        assertTrue(node.processStartTime().isBefore(DateTime.now()));
        assertTrue(node.processVirtualSize() > 0);
        assertFalse(Strings.isNullOrEmpty(node.processArguments()));
        assertFalse(Strings.isNullOrEmpty(node.osInformation()));
    }

    @Test
    public void testIdentifiesEphemeralNodes() throws NodeManager.NodeInitializationException {
        NzymeNode nzyme = new MockNzyme();
        NodeManager nm = new NodeManager(nzyme);
        nm.initialize();

        nm.registerSelf();

        assertFalse(nm.getNode(nzyme.getNodeManager().getLocalNodeId()).get().isEphemeral());

        nzyme.getDatabaseCoreRegistry().setValue(NodeRegistryKeys.EPHEMERAL_NODES_REGEX.key(), "^foo-.+");
        assertFalse(nm.getNode(nzyme.getNodeManager().getLocalNodeId()).get().isEphemeral());

        nzyme.getDatabaseCoreRegistry().setValue(NodeRegistryKeys.EPHEMERAL_NODES_REGEX.key(), "^mocky-.+");
        assertTrue(nm.getNode(nzyme.getNodeManager().getLocalNodeId()).get().isEphemeral());
    }

    @Test
    public void testGeneratesNodeLocalPublicKey() throws PGPException, IOException, Crypto.CryptoInitializationException {
        NzymeNode nzyme = new MockNzyme();
        nzyme.getNodeManager().registerSelf();
        nzyme.getCrypto().initialize(false);

        byte[] fromDatabase = nzyme.getNodeManager().getPGPPublicKeyOfNode(nzyme.getNodeManager().getLocalNodeId());
        PGPPublicKey publicFromDatabase = Crypto.readPublicKey(fromDatabase);
        assertTrue(new DateTime(publicFromDatabase.getCreationTime()).isBefore(DateTime.now()));

        byte[] fromSubsys = nzyme.getCrypto().getNodeLocalPGPKeys().publicKey();
        PGPPublicKey publicFromSubsys = Crypto.readPublicKey(fromSubsys);
        assertEquals(publicFromSubsys.getKeyID(), publicFromDatabase.getKeyID());
    }

}