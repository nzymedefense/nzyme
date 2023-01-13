package app.nzyme.core.distributed;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.database.NodeEntry;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;

public class NodeManager {

    private static final Logger LOG = LogManager.getLogger(NodeManager.class);

    private final NzymeNode nzyme;

    private UUID localNodeId;

    public NodeManager(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() throws NodeInitializationException {
        // Read local node id.
        Path nodeIdFile = Path.of(nzyme.getDataDirectory().toString(), "node_id");
        if (Files.exists(nodeIdFile)) {
            try {
                LOG.debug("Node ID file exists at [{}]", nodeIdFile.toAbsolutePath());
                localNodeId = UUID.fromString(Files.readString(nodeIdFile));
            } catch (IOException e) {
                throw new NodeInitializationException("Could not read node ID file at [" + nodeIdFile.toAbsolutePath() + "]", e);
            }
        } else {
            LOG.debug("Node ID file does not exist at [{}]. Creating.", nodeIdFile.toAbsolutePath());
            UUID newNodeId = UUID.randomUUID();

            try {
                Files.writeString(nodeIdFile, newNodeId.toString(), Charsets.UTF_8, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new NodeInitializationException("Could not write node ID file at [" + nodeIdFile.toAbsolutePath() + "]", e);
            }

            localNodeId = newNodeId;
            LOG.info("Created node ID: [{}]", localNodeId);
        }

        LOG.info("Node ID: [{}]", localNodeId);
    }

    public void registerSelf() {
        if (localNodeId == null) {
            throw new RuntimeException("Not initialized. Cannot register myself.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO nodes(uuid, name, transport_address, version, last_seen) " +
                        "VALUES(:uuid, :name, :transport_address, :version, :last_seen) " +
                        "ON CONFLICT(uuid) DO UPDATE SET name = :name, transport_address = :transport_address, " +
                        "version = :version, last_seen = :last_seen")
                        .bind("uuid", localNodeId)
                        .bind("name", nzyme.getNodeInformation().name())
                        .bind("transport_address", nzyme.getConfiguration().httpExternalUri().toString())
                        .bind("version", nzyme.getVersion().getVersion().toString())
                        .bind("last_seen", DateTime.now())
                        .execute()
        );
    }

    public List<Node> getActiveNodes() {
        List<NodeEntry> dbEntries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT uuid, name, transport_address, version, last_seen FROM nodes " +
                                "WHERE last_seen > :timeout ORDER BY name DESC")
                        .bind("timeout", DateTime.now().minusSeconds(30))
                        .mapTo(NodeEntry.class)
                        .list()
        );

        List<Node> nodes = Lists.newArrayList();
        for (NodeEntry dbEntry : dbEntries) {
            try {
                URI transportAddress = URI.create(dbEntry.transportAddress());
                nodes.add(Node.create(dbEntry.uuid(), dbEntry.name(), transportAddress, dbEntry.version(), dbEntry.lastSeen()));
            } catch (Exception e) {
                LOG.error("Could not create node from database entry. Skipping.", e);
            }
        }

        return nodes;
    }

    public UUID getLocalNodeId() {
        return localNodeId;
    }

    public static final class NodeInitializationException extends Throwable {

        public NodeInitializationException(String msg) {
            super(msg);
        }

        public NodeInitializationException(String msg, Throwable e) {
            super(msg, e);
        }

    }

}
