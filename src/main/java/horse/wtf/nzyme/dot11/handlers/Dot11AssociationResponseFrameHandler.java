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
import horse.wtf.nzyme.dot11.frames.Dot11AssociationResponseFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11AssociationResponseFrameHandler extends Dot11FrameHandler<Dot11AssociationResponseFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11AssociationResponseFrameHandler.class);

    private final RemoteConnector remote;

    public Dot11AssociationResponseFrameHandler(Dot11Probe probe, RemoteConnector remote) {
        super(probe);

        this.remote = remote;
    }

    @Override
    protected void doHandle(Dot11AssociationResponseFrame frame) {
        String message = frame.transmitter() + " answered association request from " + frame.destination()
                + ". Response: " + frame.response().toUpperCase() + " (" + frame.responseCode() + ")";

        remote.notifyUplinks(
                new Notification(message, frame.meta().getChannel())
                        .addField(FieldNames.TRANSMITTER, frame.transmitter())
                        .addField(FieldNames.DESTINATION, frame.destination())
                        .addField(FieldNames.RESPONSE_CODE, frame.responseCode())
                        .addField(FieldNames.RESPONSE_STRING, frame.response())
                        .addField(FieldNames.SUBTYPE, "assoc-resp"),
                frame.meta()
        );

        remote.forwardFrame(frame);

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "assoc-resp";
    }
}
