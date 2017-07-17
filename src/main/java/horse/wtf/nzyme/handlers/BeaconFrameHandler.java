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
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11SSID;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.concurrent.atomic.AtomicInteger;

public class BeaconFrameHandler extends FrameHandler {

    private static final int SSID_LENGTH_POSITION = 37;
    private static final int SSID_POSITION = 38;

    private final AtomicInteger sampleCount;

    private static final Logger LOG = LogManager.getLogger(BeaconFrameHandler.class);

    public BeaconFrameHandler(Nzyme nzyme) {
        super(nzyme);

        this.sampleCount = new AtomicInteger(0);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();
        if(nzyme.getConfiguration().getBeaconSamplingRate() != 0) { // skip this completely if sampling is disabled
            if (sampleCount.getAndIncrement() == nzyme.getConfiguration().getBeaconSamplingRate()) {
                sampleCount.set(0);
            } else {
                return;
            }
        }

        Dot11ManagementFrame beacon = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String ssid = null;
        try {
            ssid = Dot11SSID.extractSSID(SSID_LENGTH_POSITION, SSID_POSITION, payload);
        } catch (MalformedFrameException e) {
            malformed();
            LOG.trace("Skipping malformed beacon frame.");
        }

        String transmitter = "";
        if(beacon.getHeader().getAddress2() != null) {
            transmitter = beacon.getHeader().getAddress2().toString();
        }

        String message;
        if (ssid != null && !ssid.trim().isEmpty()) {
            message = "Received beacon from " + transmitter + " for SSID " + ssid;
            nzyme.getStatistics().tickBeaconedNetwork(ssid);
        } else {
            // Broadcast beacon.
            message = "Received broadcast beacon from " + transmitter;
        }

        nzyme.getStatistics().tickAccessPoint(transmitter);

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(GraylogFieldNames.TRANSMITTER, transmitter)
                        .addField(GraylogFieldNames.SSID, ssid == null ? "[no SSID]" : ssid)
                        .addField(GraylogFieldNames.SUBTYPE, "beacon"),
                meta
        );

        LOG.info(message);
    }

    @Override
    public String getName() {
        return "beacon";
    }

}
