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

package horse.wtf.nzyme.dot11.handlers;

import com.google.common.collect.Maps;
import horse.wtf.nzyme.RemoteConnector;
import horse.wtf.nzyme.dot11.frames.Dot11AuthenticationFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Dot11AuthenticationFrameHandler extends Dot11FrameHandler<Dot11AuthenticationFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11AuthenticationFrameHandler.class);

    private final RemoteConnector remote;

    public Dot11AuthenticationFrameHandler(Dot11Probe probe, RemoteConnector remote) {
        super(probe);

        this.remote = remote;
    }

    @Override
    protected void doHandle(Dot11AuthenticationFrame frame) {
        String message = "";
        Map<String, Object> additionalFields = Maps.newHashMap();

        switch(frame.algorithm()) {
            case OPEN_SYSTEM:
                switch(frame.transactionSequence()) {
                    case 1:
                        message = frame.transmitter() + " is requesting to authenticate with Open System (Open, WPA, WPA2, ...) " +
                                "at " + frame.destination();
                        break;
                    case 2:
                        message = frame.transmitter() + " is responding to Open System (Open, WPA, WPA2, ...) authentication " +
                                "request from " + frame.destination() + ". (" + frame.statusString() + ")";
                        additionalFields.put(FieldNames.RESPONSE_CODE, frame.statusCode());
                        additionalFields.put(FieldNames.RESPONSE_STRING, frame.statusString());
                        break;
                    default:
                        LOG.trace("Invalid Open System authentication transaction sequence number [{}]. " +
                                "Skipping.", frame.transactionSequence());
                        return;
                }
                break;
            case SHARED_KEY:
                switch (frame.transactionSequence()) {
                    case 1:
                        message = frame.transmitter() + " is requesting to authenticate using WEP at " + frame.destination();
                        break;
                    case 2:
                        message = frame.transmitter() + " is responding to WEP authentication request at " +
                                frame.destination() + " with clear text challenge.";
                        break;
                    case 4:
                        message = frame.transmitter() + " is responding to WEP authentication request from " +
                                frame.destination() + ". (" + frame.statusString() + ")";
                        additionalFields.put(FieldNames.RESPONSE_CODE, frame.statusCode());
                        additionalFields.put(FieldNames.RESPONSE_STRING, frame.statusString());
                        break;
                    default:
                        LOG.trace("Invalid WEP authentication transaction sequence number [{}]. " +
                                "Skipping.", frame.transactionSequence());
                        return;
                }
                break;
        }

        remote.notifyUplinks(new Notification(message, frame.meta().getChannel())
                .addField(FieldNames.TRANSMITTER, frame.transmitter())
                .addField(FieldNames.DESTINATION, frame.destination())
                .addField(FieldNames.AUTH_ALGORITHM, frame.algorithm().toString().toLowerCase())
                .addField(FieldNames.TRANSACTION_SEQUENCE_NUMBER, frame.transactionSequence())
                .addField(FieldNames.SUBTYPE, "auth")
                .addFields(additionalFields), frame.meta());

        remote.forwardFrame(frame);

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "auth";
    }

}
