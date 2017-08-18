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
import horse.wtf.nzyme.dot11.Dot11LeavingReason;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;

public class DisassociationFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(DisassociationFrameHandler.class);

    public DisassociationFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame disassociationRequest = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String destination = "";
        if(disassociationRequest.getHeader().getAddress1() != null) {
            destination = disassociationRequest.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(disassociationRequest.getHeader().getAddress2() != null) {
            transmitter = disassociationRequest.getHeader().getAddress2().toString();
        }

        // Reason.
        short reasonCode = Dot11LeavingReason.extract(payload, header);
        String reasonString = Dot11LeavingReason.lookup(reasonCode);

        String message = transmitter + " is disassociating from " + destination + " (" + reasonString + ")";

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(FieldNames.TRANSMITTER, transmitter)
                        .addField(FieldNames.DESTINATION, destination)
                        .addField(FieldNames.REASON_CODE, reasonCode)
                        .addField(FieldNames.REASON_STRING, reasonString)
                        .addField(FieldNames.SUBTYPE, "disassoc"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "disassoc";
    }

}
