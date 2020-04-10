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

package horse.wtf.nzyme.bandits.trackers.devices;

import horse.wtf.nzyme.bandits.trackers.TrackerMessageReceiver;
import jssc.SerialPortException;

public interface TrackerDevice {

    enum TYPE {
        SX126X_LORA
    }

    void initialize() throws TrackerDeviceInitializationException;
    void stop();

    String getTypeDescription();

    void readLoop();
    void transmit(byte[] message) throws SerialPortException;
    void onMessageReceived(TrackerMessageReceiver receiver);

    class TrackerDeviceInitializationException extends Exception {

        public TrackerDeviceInitializationException(String msg) {
            super(msg);
        }

        public TrackerDeviceInitializationException(String msg, Throwable e) {
            super(msg, e);
        }

    }

}
