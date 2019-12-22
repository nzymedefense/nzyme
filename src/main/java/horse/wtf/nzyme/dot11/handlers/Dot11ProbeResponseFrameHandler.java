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

import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Dot11ProbeResponseFrameHandler extends Dot11FrameHandler<Dot11ProbeResponseFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11ProbeResponseFrameHandler.class);

    public Dot11ProbeResponseFrameHandler(Dot11Probe probe) {
        super(probe);
    }

    @Override
    protected void doHandle(Dot11ProbeResponseFrame frame) {
        String message;
        if (frame.ssid() == null) {
            message = frame.transmitter() + " responded to broadcast probe request from " + frame.destination();
        } else {
            message = frame.transmitter() + " responded to probe request from " + frame.destination() + " for " + frame.ssid();
        }

        probe.notifyUplinksOfFrame(
                new Notification(message, frame.meta().getChannel())
                        .addField(FieldNames.DESTINATION, frame.destination())
                        .addField(FieldNames.TRANSMITTER, frame.transmitter())
                        .addField(FieldNames.SSID, frame.ssid() == null ? "[no SSID]" : frame.ssid())
                        .addField(FieldNames.SECURITY_FULL, frame.taggedParameters().getFullSecurityString())
                        .addField(FieldNames.IS_WPA1, frame.taggedParameters().isWPA1())
                        .addField(FieldNames.IS_WPA2, frame.taggedParameters().isWPA2())
                        .addField(FieldNames.IS_WPS, frame.taggedParameters().isWPS())
                        .addField(FieldNames.SUBTYPE, "probe-resp"),
                        frame.meta()
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "probe-resp";
    }

}
