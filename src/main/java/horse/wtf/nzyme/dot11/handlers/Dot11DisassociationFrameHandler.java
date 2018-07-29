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

import horse.wtf.nzyme.dot11.frames.Dot11DisassociationFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11DisassociationFrameHandler extends Dot11FrameHandler<Dot11DisassociationFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11DisassociationFrameHandler.class);

    public Dot11DisassociationFrameHandler(Dot11Probe nzyme) {
        super(nzyme);
    }

    @Override
    public void doHandle(Dot11DisassociationFrame frame) {
        String message = frame.transmitter() + " is disassociating from " + frame.destination() + " (" + frame.reasonString() + ")";

        probe.notifyUplinks(
                new Notification(message, frame.meta().getChannel())
                        .addField(FieldNames.TRANSMITTER, frame.transmitter())
                        .addField(FieldNames.DESTINATION, frame.destination())
                        .addField(FieldNames.REASON_CODE, frame.reasonCode())
                        .addField(FieldNames.REASON_STRING, frame.reasonString())
                        .addField(FieldNames.SUBTYPE, "disassoc"),
                frame.meta()
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "disassoc";
    }

}
