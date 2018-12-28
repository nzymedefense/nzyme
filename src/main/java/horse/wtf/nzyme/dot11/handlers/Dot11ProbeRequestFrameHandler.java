/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.dot11.handlers;

import horse.wtf.nzyme.dot11.frames.Dot11ProbeRequestFrame;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11ProbeRequestFrameHandler extends Dot11FrameHandler<Dot11ProbeRequestFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11ProbeRequestFrameHandler.class);

    public Dot11ProbeRequestFrameHandler(Dot11Probe probe) {
        super(probe);
    }

    @Override
    public void doHandle(Dot11ProbeRequestFrame frame) {
        String message;
        if(!frame.isBroadcastProbe()) {
            message = "Probe request: " + frame.requester() + " is looking for " + frame.ssid();
        } else {
            message = "Probe request: " + frame.requester() + " is looking for any network. (null probe request)";
        }

        probe.getStatistics().tickProbingDevice(frame.requester());

        probe.notifyUplinks(
                new Notification(message, frame.meta().getChannel(), probe)
                        .addField(FieldNames.SSID, frame.ssid())
                        .addField(FieldNames.TRANSMITTER, frame.requester())
                        .addField(FieldNames.SUBTYPE, "probe-req"),
                frame.meta()
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "probe-req";
    }

}
