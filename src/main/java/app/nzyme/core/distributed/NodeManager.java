package app.nzyme.core.distributed;

import app.nzyme.core.NzymeNode;
import com.google.common.base.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class NodeManager {

    private static final Logger LOG = LogManager.getLogger(NodeManager.class);

    private final NzymeNode nzyme;

    private UUID nodeId;

    public NodeManager(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() throws NodeInitializationException {
        // Read local node id.
        Path nodeIdFile = Path.of(nzyme.getDataDirectory().toString(), "node_id");
        if (Files.exists(nodeIdFile)) {
            try {
                LOG.debug("Node ID file exists at [{}]", nodeIdFile.toAbsolutePath());
                nodeId = UUID.fromString(Files.readString(nodeIdFile));
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

            nodeId = newNodeId;
            LOG.info("Created node ID: [{}]", nodeId);
        }

        LOG.info("Node ID: [{}]", nodeId);
    }

    public void registerSelf() {
        if (nodeId == null) {
            throw new RuntimeException("Not initialized. Cannot register myself.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO nodes(uuid, name, transport_address, last_seen) " +
                        "VALUES(:uuid, :name, :transport_address, :last_seen) " +
                        "ON CONFLICT(uuid) DO UPDATE SET " +
                        "name = :name, transport_address = :transport_address, last_seen = :last_seen")
                        .bind("uuid", nodeId)
                        .bind("name", nzyme.getNodeInformation().name())
                        .bind("transport_address", nzyme.getConfiguration().httpExternalUri().toString())
                        .bind("last_seen", DateTime.now())
        );
    }

    public UUID getNodeId() {
        return nodeId;
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
