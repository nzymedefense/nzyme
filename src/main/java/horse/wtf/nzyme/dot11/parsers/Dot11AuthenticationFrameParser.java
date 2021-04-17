/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11AuthenticationFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.nio.ByteOrder;

public class Dot11AuthenticationFrameParser extends Dot11FrameParser<Dot11AuthenticationFrame> {

    private final Anonymizer anonymizer;

    private final static int MAC_HEADER_LEN = 24;

    private final static int ALGO_NUM_LENGTH = 2;
    private final static int ALGO_NUM_POSITION = MAC_HEADER_LEN;

    private final static int TRANSACTION_SEQ_NO_LENGTH = 2;
    private final static int TRANSACTION_SEQ_NO_POSITION = MAC_HEADER_LEN + 2;

    private final static int STATUS_CODE_LENGTH = 2;
    private final static int STATUS_CODE_POSITION = MAC_HEADER_LEN + 4;

    public Dot11AuthenticationFrameParser(MetricRegistry metrics, Anonymizer anonymizer) {
        super(metrics);

        this.anonymizer = anonymizer;
    }

    public enum ALGORITHM_TYPE {
        OPEN_SYSTEM, SHARED_KEY
    }

    @Override
    protected Dot11AuthenticationFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ManagementFrame auth = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        try {
            ByteArrays.validateBounds(payload, ALGO_NUM_POSITION, ALGO_NUM_LENGTH);
            ByteArrays.validateBounds(payload, TRANSACTION_SEQ_NO_POSITION, TRANSACTION_SEQ_NO_LENGTH);
            ByteArrays.validateBounds(payload, STATUS_CODE_POSITION, STATUS_CODE_LENGTH);
        } catch(Exception e){
            throw new MalformedFrameException("Payload out of bounds. (1) Ignoring.");
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
                throw new MalformedFrameException("Invalid algorithm type with code [" + algorithmCode + "]. Skipping.");
        }

        short statusCode = ByteArrays.getShort(statusCodeArray, 0, ByteOrder.LITTLE_ENDIAN);
        String status;
        switch(statusCode) {
            case 0:
                status = "success";
                break;
            case 1:
                status = "failure";
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

        if (anonymizer.isEnabled()) {
            transmitter = anonymizer.anonymizeBSSID(transmitter);
            destination = anonymizer.anonymizeBSSID(destination);
        }

        return Dot11AuthenticationFrame.create(algorithm, statusCode, status, transactionSequence, destination, transmitter, meta, payload, header);
    }

}
