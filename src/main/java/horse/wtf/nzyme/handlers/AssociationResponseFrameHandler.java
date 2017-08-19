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
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.nio.ByteOrder;

public class AssociationResponseFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(AssociationResponseFrameHandler.class);

    private static final int STATUS_CODE_POSITION = 26;
    private static final int STATUS_CODE_LENGTH = 2;

    public AssociationResponseFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame associationResponse = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        // Check bounds for response code field.
        try {
            ByteArrays.validateBounds(payload, 0, STATUS_CODE_POSITION+STATUS_CODE_LENGTH-1);
        } catch(Exception e) {
            malformed(meta);
            LOG.trace("Payload out of bounds. (1) Ignoring.");
            return;
        }

        // Parse the response code. 0 means success any other value means failure.
        short responseCode = ByteArrays.getShort(new byte[]{payload[26], payload[27]}, 0, ByteOrder.LITTLE_ENDIAN);

        if(responseCode < 0) {
            LOG.trace("Invalid response code <{}>.", responseCode);
            return;
        }

        String response = "refused";
        if (responseCode == 0) {
            response = "success";
        }

        String destination = "";
        if(associationResponse.getHeader().getAddress1() != null) {
            destination = associationResponse.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(associationResponse.getHeader().getAddress2() != null) {
            transmitter = associationResponse.getHeader().getAddress2().toString();
        }

        String message = transmitter + " answered association request from " + destination
                + ". Response: " + response.toUpperCase() + " (" + responseCode + ")";

        nzyme.notify(
                new Notification(message, meta.getChannel())
                        .addField(FieldNames.TRANSMITTER, transmitter)
                        .addField(FieldNames.DESTINATION, destination)
                        .addField(FieldNames.RESPONSE_CODE, responseCode)
                        .addField(FieldNames.RESPONSE_STRING, response)
                        .addField(FieldNames.SUBTYPE, "assoc-resp"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "assoc-resp";
    }
}
