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

import horse.wtf.nzyme.*;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.Dot11ProbeRequestPacket;
import org.pcap4j.packet.IllegalRawDataException;

import java.text.Normalizer;

public class ProbeRequestFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public ProbeRequestFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException  {
        tick();

        Dot11ProbeRequestPacket probeRequest = Dot11ProbeRequestPacket.newPacket(payload, 0, payload.length);

        if (probeRequest.getHeader() == null) {
            malformed(meta);
            LOG.trace("Malformed header in probe request packet. Skipping.");
            return;
        }

        String ssid;
        boolean nullProbe = false;
        if (probeRequest.getHeader().getSsid() != null) {
            // Check if the SSID is valid UTF-8 (might me malformed frame)
            if(!Tools.isValidUTF8(probeRequest.getHeader().getSsid().getRawData())) {
                malformed(meta);
                LOG.trace("Malformed SSID in probe request packet. Skipping.");
                return;
            }

            ssid = Normalizer.normalize(probeRequest.getHeader().getSsid().getSsid(), Normalizer.Form.NFD);

            if (ssid.trim().isEmpty()) {
                ssid = "NULL";
                nullProbe = true;
            }
        } else {
            malformed(meta);
            LOG.trace("Malformed SSID in probe request packet. Skipping.");
            return;
        }

        String requester;
        if (probeRequest.getHeader().getAddress2() != null) {
            requester = Normalizer.normalize(probeRequest.getHeader().getAddress2().toString(), Normalizer.Form.NFD);
        } else {
            malformed(meta);
            LOG.trace("Malformed SSID in probe request packet. Skipping.");
            return;
        }

        String message;
        if(!nullProbe) {
            message = "Probe request: " + requester + " is looking for " + ssid;
        } else {
            message = "Probe request: " + requester + " is looking for any network. (null probe request)";
        }

        nzyme.getStatistics().tickProbingDevice(requester);

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(GraylogFieldNames.SSID, ssid)
                        .addField(GraylogFieldNames.TRANSMITTER, requester)
                        .addField(GraylogFieldNames.SUBTYPE, "probe-req"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "probe-req";
    }

}
