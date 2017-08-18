/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.nio.ByteOrder;

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
            malformed(meta);
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
                malformed(meta);
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
                        malformed(meta);
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
                        malformed(meta);
                        LOG.trace("Invalid WEP authentication transaction sequence number [{}]. " +
                                "Skipping.", transactionSequence);
                        return;
                }
                break;
        }

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(FieldNames.TRANSMITTER, transmitter)
                        .addField(FieldNames.DESTINATION, destination)
                        .addField(FieldNames.RESPONSE_CODE, statusCode)
                        .addField(FieldNames.RESPONSE_STRING, status)
                        .addField(FieldNames.AUTH_ALGORITHM, algorithm.toString().toLowerCase())
                        .addField(FieldNames.TRANSACTION_SEQUENCE_NUMBER, transactionSequence)
                        .addField(FieldNames.IS_WEP, algorithm.equals(ALGORITHM_TYPE.SHARED_KEY))
                        .addField(FieldNames.SUBTYPE, "auth"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "auth";
    }

}
