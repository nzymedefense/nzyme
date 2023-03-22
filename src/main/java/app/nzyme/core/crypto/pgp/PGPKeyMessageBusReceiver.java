package app.nzyme.core.crypto.pgp;

import app.nzyme.core.crypto.Crypto;
import app.nzyme.plugin.distributed.messaging.MessageHandler;
import app.nzyme.plugin.distributed.messaging.MessageProcessingResult;
import app.nzyme.plugin.distributed.messaging.ReceivedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;

public class PGPKeyMessageBusReceiver implements MessageHandler {

    private static final Logger LOG = LogManager.getLogger(PGPKeyMessageBusReceiver.class);

    private final Crypto crypto;
    private final File cryptoDirectoryConfig;
    private final ObjectMapper om;

    public PGPKeyMessageBusReceiver(File cryptoDirectory, Crypto crypto) {
        this.crypto = crypto;
        this.cryptoDirectoryConfig = cryptoDirectory;
        this.om = new ObjectMapper();
    }

    @Override
    public MessageProcessingResult handle(ReceivedMessage message) {
        try {
            LOG.info("Received [{}] from [{}].", message.type(), message.sender());

            PGPKeyMessagePayload payload = om.readValue(message.parametersString(), PGPKeyMessagePayload.class);

            // TODO decrypt.
            // Write keys to files.
            File privateKeyLocation = Paths.get(cryptoDirectoryConfig.toString(),
                    Crypto.PGP_PRIVATE_KEY_FILE_NAME).toFile();
            File publicKeyLocation = Paths.get(cryptoDirectoryConfig.toString(),
                    Crypto.PGP_PUBLIC_KEY_FILE_NAME).toFile();

            byte[] privateKey = BaseEncoding.base64().decode(payload.privateKey());
            byte[] publicKey = BaseEncoding.base64().decode(payload.publicKey());

            Files.write(privateKey, privateKeyLocation);
            Files.write(publicKey, publicKeyLocation);

            return MessageProcessingResult.SUCCESS;
        } catch(Exception e) {
            LOG.info("Could not process received PGP key.", e);
            return MessageProcessingResult.FAILURE;
        }
    }

    @Override
    public String getName() {
        return "PGP Key Receiver";
    }

}
