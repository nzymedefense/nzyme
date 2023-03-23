package app.nzyme.core.crypto.pgp;

import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.distributed.NodeManager;
import app.nzyme.plugin.distributed.messaging.Message;
import app.nzyme.plugin.distributed.messaging.MessageBus;
import app.nzyme.plugin.distributed.messaging.MessageType;
import app.nzyme.plugin.distributed.tasksqueue.ReceivedTask;
import app.nzyme.plugin.distributed.tasksqueue.TaskHandler;
import app.nzyme.plugin.distributed.tasksqueue.TaskProcessingResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class PGPKeyProviderTaskHandler implements TaskHandler {

    private static final Logger LOG = LogManager.getLogger(PGPKeyMessageBusReceiver.class);

    private final File cryptoDirectoryConfig;
    private final MessageBus messageBus;
    private final NodeManager nodeManager;
    private final Crypto crypto;
    private final ObjectMapper om;

    public PGPKeyProviderTaskHandler(File cryptoDirectory, MessageBus messageBus, NodeManager nodeManager, Crypto crypto) {
        this.cryptoDirectoryConfig = cryptoDirectory;
        this.messageBus = messageBus;
        this.nodeManager = nodeManager;
        this.crypto = crypto;
        this.om = new ObjectMapper();
    }

    @Override
    public TaskProcessingResult handle(ReceivedTask task) {
        try {
            if (!crypto.isPGPKeySyncEnabled()) {
                // Really, it should never even come this far, but why not check.
                LOG.warn("PGP sync is disabled. Not sending keys.");
                return TaskProcessingResult.FAILURE;
            }

            LOG.info("Responding to PGP key request by [{}].", task.senderNodeId());
            LOG.info("Loading keys from disk.");

            // Read keys from files.
            File privateKeyLocation = Paths.get(cryptoDirectoryConfig.toString(),
                    Crypto.PGP_PRIVATE_KEY_FILE_NAME).toFile();
            File publicKeyLocation = Paths.get(cryptoDirectoryConfig.toString(),
                    Crypto.PGP_PUBLIC_KEY_FILE_NAME).toFile();

            byte[] privateKey, publicKey;
            if (privateKeyLocation.canRead() && publicKeyLocation.canRead()) {
                privateKey = Files.readAllBytes(privateKeyLocation.toPath());
                publicKey = Files.readAllBytes(publicKeyLocation.toPath());
            } else {
                throw new RuntimeException("Could not read PGP keys from disk.");
            }

            // Encrypt keys using public key from requesting node.
            LOG.info("Encrypting keys with public key of node [{}]", task.senderNodeId());
            PGPPublicKey nodeKey = Crypto.readPublicKey(nodeManager.getPGPPublicKeyOfNode(task.senderNodeId()));
            byte[] encryptedPrivateKey = crypto.encrypt(privateKey, nodeKey);
            byte[] encryptedPublicKey = crypto.encrypt(publicKey, nodeKey);
            String encryptedPrivateKeyEncoded = BaseEncoding.base64().encode(encryptedPrivateKey);
            String encryptedPublicKeyEncoded = BaseEncoding.base64().encode(encryptedPublicKey);
            PGPKeyMessagePayload payload = PGPKeyMessagePayload.create(encryptedPublicKeyEncoded, encryptedPrivateKeyEncoded);
            Map<String, Object> parameters = this.om.convertValue(payload, new TypeReference<>() {});

            LOG.info("Keys encrypted and encoded. Publishing message to requesting node.");

            // Publish keys.
            messageBus.send(Message.create(
                    task.senderNodeId(),
                    MessageType.CLUSTER_PGP_KEYS_PROVIDED,
                    parameters,
                    true
            ));

            LOG.info("Message published. Task complete.");
            return TaskProcessingResult.SUCCESS;
        } catch(Exception | Crypto.CryptoOperationException e) {
            LOG.error("Could not respond to PGP key request.", e);
            return TaskProcessingResult.FAILURE;
        }
    }

    @Override
    public String getName() {
        return "PGP Key Provider";
    }
}
