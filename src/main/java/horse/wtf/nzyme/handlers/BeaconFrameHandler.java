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

package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11SSID;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.probes.dot11.Dot11Probe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;

public class BeaconFrameHandler extends FrameHandler {

    private static final int SSID_LENGTH_POSITION = 37;
    private static final int SSID_POSITION = 38;

    private static final Logger LOG = LogManager.getLogger(BeaconFrameHandler.class);

    public BeaconFrameHandler(Dot11Probe nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame beacon = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String ssid = null;
        try {
            ssid = Dot11SSID.extractSSID(SSID_LENGTH_POSITION, SSID_POSITION, payload);
        } catch (MalformedFrameException e) {
            malformed(meta);
            LOG.trace("Skipping malformed beacon frame.");
        }

        String transmitter = "";
        if(beacon.getHeader().getAddress2() != null) {
            transmitter = beacon.getHeader().getAddress2().toString();
        }

        String message;
        if (ssid != null && !ssid.trim().isEmpty()) {
            message = "Received beacon from " + transmitter + " for SSID " + ssid;
            probe.getStatistics().tickBeaconedNetwork(ssid);
        } else {
            // Broadcast beacon.
            message = "Received broadcast beacon from " + transmitter;
        }

        probe.getStatistics().tickAccessPoint(transmitter);

        probe.notifyUplinks(
                new Notification(message, meta.getChannel())
                        .addField(FieldNames.TRANSMITTER, transmitter)
                        .addField(FieldNames.SSID, ssid == null ? "[no SSID]" : ssid)
                        .addField(FieldNames.SUBTYPE, "beacon"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "beacon";
    }

}
