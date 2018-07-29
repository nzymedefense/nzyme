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

import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11BeaconFrameHandler extends Dot11FrameHandler<Dot11BeaconFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11BeaconFrameHandler.class);

    public Dot11BeaconFrameHandler(Dot11Probe probe) {
        super(probe);
    }

    @Override
    public void doHandle(Dot11BeaconFrame beacon) {
        String message;
        if (beacon.ssid() != null && !beacon.ssid().trim().isEmpty()) {
            message = "Received beacon from " + beacon.transmitter() + " for SSID " + beacon.ssid();
            probe.getStatistics().tickBeaconedNetwork(beacon.ssid());
        } else {
            // Broadcast beacon.
            message = "Received broadcast beacon from " + beacon.transmitter();
        }

        probe.getStatistics().tickAccessPoint(beacon.transmitter());

        probe.notifyUplinks(
                new Notification(message, beacon.meta().getChannel())
                        .addField(FieldNames.TRANSMITTER, beacon.transmitter())
                        .addField(FieldNames.SSID, beacon.ssid() == null ? "[no SSID]" : beacon.ssid())
                        .addField(FieldNames.SUBTYPE, "beacon"),
                beacon.meta()
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "beacon";
    }

}
