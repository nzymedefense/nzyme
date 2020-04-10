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

import com.google.protobuf.InvalidProtocolBufferException;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.WrapperMessageHandler;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SX126XLoRaHat implements TrackerDevice {

    private static final Logger LOG = LogManager.getLogger(SX126XLoRaHat.class);

    private final String portName;

    private SerialPort serialPort;
    private WrapperMessageHandler messageHandler = null;

    public SX126XLoRaHat(String portName) {
        this.portName = portName;
    }

    @Override
    public void initialize() throws TrackerDeviceInitializationException {
        if (this.messageHandler == null) {
            throw new TrackerDeviceInitializationException("No message receiver registered.");
        }

        try {
            // Build an initial handle.
            handle();
        } catch (SerialPortException e) {
            throw new TrackerDeviceInitializationException("Could not connect to serial port.", e);
        }

        LOG.info("Fully initialized [{}].", getTypeDescription());
    }

    @Override
    public void stop() {
        if (this.serialPort != null && this.serialPort.isOpened()) {
            try {
                this.serialPort.closePort();
            } catch (SerialPortException e) {
                LOG.warn("Could not close serial port.", e);
            }
        }
    }

    @Override
    public void readLoop() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        short nulCount = 0;
        while(true) {
            try {
                byte[] b = handle().readBytes();

                if (b != null) {
                    if (b[0] == 0x00) {
                        // Our frames end with three NULL bytes. Register but don't handle further because it's not part of the payload.
                        nulCount++;

                        // Message is complete!
                        if (nulCount == 2) {
                            nulCount = 0;
                            try {
                                messageHandler.handle(TrackerMessage.Wrapper.parseFrom(buffer.toByteArray()));
                            } catch (InvalidProtocolBufferException e) {
                                LOG.error("Skipping invalid protobuf message.", e);
                            }
                            buffer.reset();

                        }

                        continue;
                    }

                    if (nulCount > 0) {
                        // Reset NULL bytes if the three bytes are not in order. Reset counter but handle further because it's part of payload.
                        nulCount = 0;
                    }

                    try {
                        buffer.write(b);
                    } catch (IOException e) {
                        LOG.warn("Could not write to buffer.", e);
                        buffer.reset();
                        break;
                    }
                }
            } catch(SerialPortException e) {
                LOG.warn("Error in read loop.", e);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) { }
            }
        }
    }

    private SerialPort handle() throws SerialPortException {
        if (serialPort == null || !serialPort.isOpened()) {
            serialPort = new SerialPort(this.portName);
            serialPort.openPort();
            serialPort.setParams(2400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        }

        return serialPort;
    }

    @Override
    public String getTypeDescription() {
        return "Waveshare SX126X LoRa HAT";
    }

    @Override
    public void transmit(byte[] message) throws SerialPortException {
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            payload.write(message);
            payload.write(0x00);
            payload.write(0x00);
            payload.write(0x00);

            handle().writeBytes(payload.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(WrapperMessageHandler receiver) {
        this.messageHandler = receiver;
    }

}
