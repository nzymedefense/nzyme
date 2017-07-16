package horse.wtf.nzyme.handlers;

import com.beust.jcommander.internal.Maps;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.nio.ByteOrder;
import java.util.Map;

public class AuthenticationFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(AuthenticationFrameHandler.class);

    private final static int MAC_HEADER_LEN = 24;

    private final static int ALGO_NUM_LENGTH = 2;
    private final static int ALGO_NUM_POSITION = MAC_HEADER_LEN;

    private final static int TRANSACTION_SEQ_NO_LENGTH = 2;
    private final static int TRANSACTION_SEQ_NO_POSITION = MAC_HEADER_LEN + 2;

    private final static int STATUS_CODE_LENGTH = 2;
    private final static int STATUS_CODE_POSITION = MAC_HEADER_LEN + 4;

    enum ALGORITHM_TYPE {
            OPEN_SYSTEM, SHARED_KEY
    }

    public AuthenticationFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame auth = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        try {
            ByteArrays.validateBounds(payload, ALGO_NUM_POSITION, ALGO_NUM_LENGTH);
            ByteArrays.validateBounds(payload, TRANSACTION_SEQ_NO_POSITION, TRANSACTION_SEQ_NO_LENGTH);
            ByteArrays.validateBounds(payload, STATUS_CODE_POSITION, STATUS_CODE_LENGTH);
        } catch(Exception e){
            malformed();
            LOG.trace("Payload out of bounds. (1) Ignoring.");
            return;
        }

        byte[] algoNumArray = ByteArrays.getSubArray(payload, ALGO_NUM_POSITION, ALGO_NUM_LENGTH);
        byte[] transactionSeqArray = ByteArrays.getSubArray(payload, TRANSACTION_SEQ_NO_POSITION, TRANSACTION_SEQ_NO_LENGTH);
        byte[] statusCodeArray = ByteArrays.getSubArray(payload, STATUS_CODE_POSITION, STATUS_CODE_LENGTH);

        short algorithmCode = ByteArrays.getShort(algoNumArray, 0, ByteOrder.LITTLE_ENDIAN);
        ALGORITHM_TYPE algorithm;
        switch(algorithmCode) {
            case 0:
                algorithm = ALGORITHM_TYPE.OPEN_SYSTEM;
                break;
            case 1:
                algorithm = ALGORITHM_TYPE.SHARED_KEY;
                break;
            default:
                malformed();
                LOG.trace("Invalid algorithm type with code [{}]. Skipping.", algorithmCode);
                return;
        }

        short statusCode = ByteArrays.getShort(statusCodeArray, 0, ByteOrder.LITTLE_ENDIAN);
        String status;
        switch(statusCode) {
            case 0:
                status = "SUCCESS";
                break;
            case 1:
                status = "FAILURE";
                break;
            default:
                status = "Invalid/Unknown (" + statusCode + ")";
                break;
        }

        short transactionSequence = ByteArrays.getShort(transactionSeqArray, 0, ByteOrder.LITTLE_ENDIAN);

        String destination = "";
        if (auth.getHeader().getAddress1() != null) {
            destination = auth.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if (auth.getHeader().getAddress2() != null) {
            transmitter = auth.getHeader().getAddress2().toString();
        }
        
        String message = "";
        switch(algorithm) {
            case OPEN_SYSTEM:
                switch(transactionSequence) {
                    case 1:
                        message = transmitter + " is requesting to authenticate with Open System (WPA, WPA2, ...) " +
                                "at " + destination;
                        break;
                    case 2:
                        message = transmitter + " is responding to Open System (WPA, WPA2, ...) authentication " +
                                "request from " + destination + ". (" + status + ")";
                        break;
                    default:
                        malformed();
                        LOG.trace("Invalid Open System authentication transaction sequence number [{}]. " +
                                "Skipping.", transactionSequence);
                        return;
                }
                break;
            case SHARED_KEY:
                switch (transactionSequence) {
                    case 1:
                        message = transmitter + " is requesting to authenticate using WEP at " + destination;
                        break;
                    case 2:
                        message = transmitter + " is responding to WEP authentication request at " +
                                destination + " with clear text challenge.";
                        break;
                    case 3:
                        message = transmitter + " is responding to WEP authentication request clear text " +
                                "challenge from " + destination;
                        break;
                    case 4:
                        message = transmitter + " is responding to WEP authentication request from " +
                                destination + ". (" + status + ")";
                        break;
                    default:
                        malformed();
                        LOG.trace("Invalid WEP authentication transaction sequence number [{}]. " +
                                "Skipping.", transactionSequence);
                        return;
                }
                break;
        }

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(GraylogFieldNames.TRANSMITTER, transmitter)
                        .addField(GraylogFieldNames.DESTINATION, destination)
                        .addField(GraylogFieldNames.RESPONSE_CODE, statusCode)
                        .addField(GraylogFieldNames.RESPONSE_STRING, status)
                        .addField(GraylogFieldNames.AUTH_ALGORITHM, algorithm.toString().toLowerCase())
                        .addField(GraylogFieldNames.TRANSACTION_SEQUENCE_NUMBER, transactionSequence)
                        .addField(GraylogFieldNames.IS_WEP, algorithm.equals(ALGORITHM_TYPE.SHARED_KEY))
                        .addField(GraylogFieldNames.SUBTYPE, "auth"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "auth";
    }

}
