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
import horse.wtf.nzyme.dot11.frames.Dot11ProbeRequestFrame;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11ProbeRequestFrameHandler extends Dot11FrameHandler<Dot11ProbeRequestFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11ProbeRequestFrameHandler.class);

    private final RemoteConnector remote;

    public Dot11ProbeRequestFrameHandler(Dot11Probe probe, RemoteConnector remote) {
        super(probe);

        this.remote = remote;
    }

    @Override
    protected void doHandle(Dot11ProbeRequestFrame frame) {
        String message;
        if(!frame.isBroadcastProbe()) {
            message = "Probe request: " + frame.requester() + " is looking for " + frame.ssid();
        } else {
            message = "Probe request: " + frame.requester() + " is looking for any network. (null probe request)";
        }

        remote.notifyUplinks(
                new Notification(message, frame.meta().getChannel())
                        .addField(FieldNames.SSID, frame.ssid() == null ? "[no SSID]" : frame.ssid())
                        .addField(FieldNames.TRANSMITTER, frame.requester())
                        .addField(FieldNames.SUBTYPE, "probe-req"),
                frame.meta()
        );

        remote.forwardFrame(frame);

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "probe-req";
    }

}
