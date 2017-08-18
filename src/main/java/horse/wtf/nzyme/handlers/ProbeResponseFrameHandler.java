/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11SSID;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;

public class ProbeResponseFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(ProbeResponseFrameHandler.class);

    public static final int SSID_LENGTH_POSITION = 37;
    public static final int SSID_POSITION = 38;

    public ProbeResponseFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame probeReponse = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String ssid = null;
        try {
            ssid = Dot11SSID.extractSSID(SSID_LENGTH_POSITION, SSID_POSITION, payload);
        } catch (MalformedFrameException e) {
            malformed();
            LOG.trace("Skipping malformed probe-resp frame.");
        }

        if (ssid == null) {
            ssid = "[no SSID]";
        }

        String destination = "";
        if (probeReponse.getHeader().getAddress1() != null) {
            destination = probeReponse.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if (probeReponse.getHeader().getAddress2() != null) {
            transmitter = probeReponse.getHeader().getAddress2().toString();
        }

        String message = transmitter + " responded to probe request from " + destination + " for " + ssid;

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(FieldNames.DESTINATION, destination)
                        .addField(FieldNames.TRANSMITTER, transmitter)
                        .addField(FieldNames.SSID, ssid)
                        .addField(FieldNames.SUBTYPE, "probe-resp"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "probe-resp";
    }

}
