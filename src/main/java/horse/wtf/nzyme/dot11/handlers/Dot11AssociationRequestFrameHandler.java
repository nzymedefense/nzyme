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

import horse.wtf.nzyme.dot11.frames.Dot11AssociationRequestFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11AssociationRequestFrameHandler extends Dot11FrameHandler<Dot11AssociationRequestFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11AssociationRequestFrameHandler.class);

    public Dot11AssociationRequestFrameHandler(Dot11Probe probe) {
        super(probe);
    }

    @Override
    public void doHandle(Dot11AssociationRequestFrame associationRequest) {
        String message = associationRequest.transmitter() + " is requesting to associate with "
                + associationRequest.ssid()
                + " at " + associationRequest.destination();

        probe.notifyUplinks(
                new Notification(message, associationRequest.meta().getChannel(), probe)
                        .addField(FieldNames.TRANSMITTER, associationRequest.transmitter())
                        .addField(FieldNames.DESTINATION, associationRequest.destination())
                        .addField(FieldNames.SSID, associationRequest.ssid() == null ? "[no SSID]" : associationRequest.ssid())
                        .addField(FieldNames.SUBTYPE, "assoc-req"),
                associationRequest.meta()
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "assoc-req";
    }

}
