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

import horse.wtf.nzyme.RemoteConnector;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11DeauthenticationFrameHandler extends Dot11FrameHandler<Dot11DeauthenticationFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11DeauthenticationFrameHandler.class);

    private final RemoteConnector remote;

    public Dot11DeauthenticationFrameHandler(Dot11Probe probe, RemoteConnector remote) {
        super(probe);

        this.remote = remote;
    }

    @Override
    protected void doHandle(Dot11DeauthenticationFrame frame) {
        String message = "Deauth: Transmitter " + frame.transmitter() + " is deauthenticating " + frame.destination()
                + " from BSSID " + frame.bssid() + " (" + frame.reasonString() + ")";

        remote.notifyUplinks(
                new Notification(message, frame.meta().getChannel())
                        .addField(FieldNames.TRANSMITTER, frame.transmitter())
                        .addField(FieldNames.DESTINATION, frame.destination())
                        .addField(FieldNames.BSSID, frame.bssid())
                        .addField(FieldNames.REASON_CODE, frame.reasonCode())
                        .addField(FieldNames.REASON_STRING, frame.reasonString())
                        .addField(FieldNames.SUBTYPE, "deauth"),
                frame.meta()
        );

        remote.forwardFrame(frame);

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "deauth";
    }

}
