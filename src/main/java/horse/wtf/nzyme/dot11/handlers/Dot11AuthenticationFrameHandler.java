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

import horse.wtf.nzyme.dot11.frames.Dot11AuthenticationFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11AuthenticationFrameParser;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dot11AuthenticationFrameHandler extends Dot11FrameHandler<Dot11AuthenticationFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11AuthenticationFrameHandler.class);

    public Dot11AuthenticationFrameHandler(Dot11Probe probe) {
        super(probe);
    }

    @Override
    public void doHandle(Dot11AuthenticationFrame frame) {
        String message = "";
        switch(frame.algorithm()) {
            case OPEN_SYSTEM:
                switch(frame.transactionSequence()) {
                    case 1:
                        message = frame.transmitter() + " is requesting to authenticate with Open System (WPA, WPA2, ...) " +
                                "at " + frame.destination();
                        break;
                    case 2:
                        message = frame.transmitter() + " is responding to Open System (WPA, WPA2, ...) authentication " +
                                "request from " + frame.destination() + ". (" + frame.statusString() + ")";
                        break;
                    default:
                        LOG.trace("Invalid Open System authentication transaction sequence number [{}]. " +
                                "Skipping.", frame.transactionSequence());
                        return;
                }
                break;
            case SHARED_KEY:
                switch (frame.transactionSequence()) {
                    case 1:
                        message = frame.transmitter() + " is requesting to authenticate using WEP at " + frame.destination();
                        break;
                    case 2:
                        message = frame.transmitter() + " is responding to WEP authentication request at " +
                                frame.destination() + " with clear text challenge.";
                        break;
                    case 3:
                        message = frame.transmitter() + " is responding to WEP authentication request clear text " +
                                "challenge from " + frame.destination();
                        break;
                    case 4:
                        message = frame.transmitter() + " is responding to WEP authentication request from " +
                                frame.destination() + ". (" + frame.statusString() + ")";
                        break;
                    default:
                        LOG.trace("Invalid WEP authentication transaction sequence number [{}]. " +
                                "Skipping.", frame.transactionSequence());
                        return;
                }
                break;
        }

        probe.notifyUplinks(
                new Notification(message, frame.meta().getChannel(), probe)
                        .addField(FieldNames.TRANSMITTER, frame.transmitter())
                        .addField(FieldNames.DESTINATION, frame.destination())
                        .addField(FieldNames.RESPONSE_CODE, frame.statusCode())
                        .addField(FieldNames.RESPONSE_STRING, frame.statusString())
                        .addField(FieldNames.AUTH_ALGORITHM, frame.algorithm().toString().toLowerCase())
                        .addField(FieldNames.TRANSACTION_SEQUENCE_NUMBER, frame.transactionSequence())
                        .addField(FieldNames.IS_WEP, frame.algorithm().equals(Dot11AuthenticationFrameParser.ALGORITHM_TYPE.SHARED_KEY))
                        .addField(FieldNames.SUBTYPE, "auth"),
                frame.meta()
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "auth";
    }

}
