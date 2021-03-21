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

import horse.wtf.nzyme.UplinkHandler;
import horse.wtf.nzyme.dot11.frames.Dot11DisassociationFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11DisassociationFrameHandler extends Dot11FrameHandler<Dot11DisassociationFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11DisassociationFrameHandler.class);

    private final UplinkHandler uplink;

    public Dot11DisassociationFrameHandler(Dot11Probe probe, UplinkHandler uplink) {
        super(probe);

        this.uplink = uplink;
    }

    @Override
    protected void doHandle(Dot11DisassociationFrame frame) {
        String message = frame.transmitter() + " is disassociating from " + frame.destination() + " (" + frame.reasonString() + ")";

        uplink.notifyUplinks(
                new Notification(message, frame.meta().getChannel())
                        .addField(FieldNames.TRANSMITTER, frame.transmitter())
                        .addField(FieldNames.DESTINATION, frame.destination())
                        .addField(FieldNames.REASON_CODE, frame.reasonCode())
                        .addField(FieldNames.REASON_STRING, frame.reasonString())
                        .addField(FieldNames.SUBTYPE, "disassoc"),
                frame.meta()
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "disassoc";
    }

}
