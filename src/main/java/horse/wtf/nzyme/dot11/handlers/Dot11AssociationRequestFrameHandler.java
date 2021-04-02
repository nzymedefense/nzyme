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
import horse.wtf.nzyme.dot11.frames.Dot11AssociationRequestFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11AssociationRequestFrameHandler extends Dot11FrameHandler<Dot11AssociationRequestFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11AssociationRequestFrameHandler.class);

    private final RemoteConnector remote;

    public Dot11AssociationRequestFrameHandler(Dot11Probe probe, RemoteConnector remote) {
        super(probe);

        this.remote = remote;
    }

    @Override
    protected void doHandle(Dot11AssociationRequestFrame associationRequest) {
        String message = associationRequest.transmitter() + " is requesting to associate with "
                + associationRequest.ssid()
                + " at " + associationRequest.destination();

        remote.notifyUplinks(
                new Notification(message, associationRequest.meta().getChannel())
                        .addField(FieldNames.TRANSMITTER, associationRequest.transmitter())
                        .addField(FieldNames.DESTINATION, associationRequest.destination())
                        .addField(FieldNames.SSID, associationRequest.ssid() == null ? "[no SSID]" : associationRequest.ssid())
                        .addField(FieldNames.SUBTYPE, "assoc-req"),
                associationRequest.meta()
        );

        remote.forwardFrame(associationRequest);

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "assoc-req";
    }

}
