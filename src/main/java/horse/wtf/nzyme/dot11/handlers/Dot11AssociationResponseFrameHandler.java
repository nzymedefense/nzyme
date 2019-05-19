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

import horse.wtf.nzyme.dot11.frames.Dot11AssociationResponseFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11AssociationResponseFrameHandler extends Dot11FrameHandler<Dot11AssociationResponseFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11AssociationResponseFrameHandler.class);

    public Dot11AssociationResponseFrameHandler(Dot11Probe probe) {
        super(probe);
    }

    @Override
    protected void doHandle(Dot11AssociationResponseFrame frame) {
        String message = frame.transmitter() + " answered association request from " + frame.destination()
                + ". Response: " + frame.response().toUpperCase() + " (" + frame.responseCode() + ")";

        probe.notifyUplinks(
                new Notification(message, frame.meta().getChannel(), probe)
                        .addField(FieldNames.TRANSMITTER, frame.transmitter())
                        .addField(FieldNames.DESTINATION, frame.destination())
                        .addField(FieldNames.RESPONSE_CODE, frame.responseCode())
                        .addField(FieldNames.RESPONSE_STRING, frame.response())
                        .addField(FieldNames.SUBTYPE, "assoc-resp"),
                frame.meta()
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "assoc-resp";
    }
}
