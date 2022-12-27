/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.bandits.trackers.devices;

import app.nzyme.core.bandits.trackers.messagehandlers.WrapperMessageHandler;
import jssc.SerialPortException;

public interface TrackerDevice {

    enum TYPE {
        SX126X_LORA
    }

    void initialize() throws TrackerDeviceInitializationException;
    void stop();

    boolean isHealthy();

    String getTypeDescription();

    void readLoop();
    void transmit(byte[] message) throws SerialPortException;
    void onMessageReceived(WrapperMessageHandler receiver);

    class TrackerDeviceInitializationException extends Exception {

        public TrackerDeviceInitializationException(String msg) {
            super(msg);
        }

        public TrackerDeviceInitializationException(String msg, Throwable e) {
            super(msg, e);
        }

    }

}
