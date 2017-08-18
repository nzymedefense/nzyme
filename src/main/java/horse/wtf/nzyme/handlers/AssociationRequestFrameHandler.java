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

public class AssociationRequestFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(AssociationRequestFrameHandler.class);

    private static final int SSID_LENGTH_POSITION = 29;
    private static final int SSID_POSITION = 30;

    public AssociationRequestFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame associationRequest = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String destination = "";
        if(associationRequest.getHeader().getAddress1() != null) {
            destination = associationRequest.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(associationRequest.getHeader().getAddress2() != null) {
            transmitter = associationRequest.getHeader().getAddress2().toString();
        }

        String ssid = null;
        try {
            ssid = Dot11SSID.extractSSID(SSID_LENGTH_POSITION, SSID_POSITION, payload);
        } catch (MalformedFrameException e) {
            malformed();
            LOG.trace("Skipping malformed assoc-req frame.");
        }

        if (ssid == null) {
            ssid = "[no SSID]";
        }

        String message = transmitter + " is requesting to associate with " + ssid + " at " + destination;

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(FieldNames.TRANSMITTER, transmitter)
                        .addField(FieldNames.DESTINATION, destination)
                        .addField(FieldNames.SSID, ssid)
                        .addField(FieldNames.SUBTYPE, "assoc-req"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "assoc-req";
    }

}
