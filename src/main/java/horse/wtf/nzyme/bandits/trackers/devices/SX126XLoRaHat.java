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

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.util.Tools;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SX126XLoRaHat implements TrackerDevice {

    public enum ParityBit {
        PB_8N1,
        PB_8O1,
        PB_8E1
    }

    public enum TransmitMode {
        TRANSPARENT,
        P2P
    }

    private static final Logger LOG = LogManager.getLogger(SX126XLoRaHat.class);

    private final String portName;

    private SerialPort serialPort;

    private int channel = 0;
    private Speeds speeds = null;
    private short transmitPower = 0;
    private ModeSettings modeSettings = null;

    private boolean initialized = false;

    public SX126XLoRaHat(String portName) {
        this.portName = portName;
    }

    @Override
    public void initialize() throws TrackerDeviceInitializationException {
        // TODO can we find out if we are in register mode or not?
        /*try {
            LOG.info("Setting [{}] registers to our default values.", getTypeDescription());
            writeChannel(18);
            writeDefaultSpeeds();
            writeDefaultModes();
            writeDefaultTransmitPower();
        } catch (SerialPortException e) {
            throw new TrackerDeviceInitializationException("Could not write initial configuration.", e);
        }

        LOG.info("START");
        try {
            channel = readChannel();
            speeds = readSpeeds();
            transmitPower = readTransmitPower();
            modeSettings = readModeSettings();
        } catch (SerialPortException e) {
            throw new TrackerDeviceInitializationException("Could not read initial configuration.", e);
        }
        LOG.info("DONE");*/

        initialized = true;

        //LOG.info(getModeDescription());
        LOG.info("Fully initialized [{}].", getTypeDescription());

        readLoop(); // TODO start outside, pass to callback
    }

    private SerialPort handle() throws SerialPortException {
        if (serialPort == null || !serialPort.isOpened()) {
            serialPort = new SerialPort(this.portName);
            serialPort.openPort();
            serialPort.setParams(2400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        }

        return serialPort;
    }

    private void readLoop() {
        while(true) {
            try {
                byte[] b = handle().readBytes();

                if (b != null) {
                    LOG.info("RECEIVED: {}", new String(b));
                }
            } catch(SerialPortException e) {
                LOG.warn("Error in read loop.", e);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) { }
            }
        }
    }

    private int readChannel() throws SerialPortException {
        handle().writeBytes(new byte[]{(byte) 0xc1, (byte) 0x05, (byte) 0x01});
        byte[] reply = serialPort.readBytes(4);

        LOG.debug("readChannel() reply: {}", Tools.byteArrayToHexPrettyPrint(reply));

        if (reply.length != 4) {
            throw new RuntimeException("Unexpected reply: " + Tools.byteArrayToHexPrettyPrint(reply));
        }

        return reply[3];
    }

    private Speeds readSpeeds() throws SerialPortException {
        handle().writeBytes(new byte[]{(byte) 0xc1, (byte) 0x03, (byte) 0x01});
        byte[] reply = serialPort.readBytes(4);

        LOG.debug("readAirSpeed() reply: {}", Tools.byteArrayToHexPrettyPrint(reply));

        if (reply.length != 4) {
            throw new RuntimeException("Unexpected reply: " + Tools.byteArrayToHexPrettyPrint(reply));
        }

        int airSpeed;
        switch(reply[3] & 0b11100000) {
            case 0b00000000:
                airSpeed = 300;
                break;
            case 0b00100000:
                airSpeed = 1200;
                break;
            case 0b01000000:
                airSpeed = 2400;
                break;
            case 0b01100000:
                airSpeed = 4800;
                break;
            case 0b10000000:
                airSpeed = 9600;
                break;
            case 0b10100000:
                airSpeed = 19200;
                break;
            case 0b11000000:
                airSpeed = 38400;
                break;
            case 0b11100000:
                airSpeed = 62500;
                break;
            default:
                throw new IllegalStateException("Unexpected air speed value");
        }

        ParityBit parityBit;
        switch(reply[3] & 0b00011000) {
            case 0b00000000:
            case 0b00011000:
                parityBit = ParityBit.PB_8N1;
                break;
            case 0b00001000:
                parityBit = ParityBit.PB_8O1;
                break;
            case 0b00010000:
                parityBit = ParityBit.PB_8E1;
                break;
            default:
                throw new IllegalStateException("Unexpected parity bit value");
        }

        int baudRate;
        switch(reply[3] & 0b00000111) {
            case 0b00000000:
                baudRate = 1200;
                break;
            case 0b00000001:
                baudRate = 2400;
                break;
            case 0b00000010:
                baudRate = 4800;
                break;
            case 0b00000011:
                baudRate = 9600;
                break;
            case 0b00000100:
                baudRate = 19200;
                break;
            case 0b00000101:
                baudRate = 38400;
                break;
            case 0b00000110:
                baudRate = 57600;
                break;
            case 0b00000111:
                baudRate = 115200;
                break;
            default:
                throw new IllegalStateException("Unexpected baud rate value");
        }

        return Speeds.create(airSpeed, parityBit, baudRate);
    }

    public short readTransmitPower() throws SerialPortException {
        handle().writeBytes(new byte[]{(byte) 0xc1, (byte) 0x04, (byte) 0x01});
        byte[] reply = serialPort.readBytes(4);

        LOG.debug("readTransmitPower() reply: {}", Tools.byteArrayToHexPrettyPrint(reply));

        if (reply.length != 4) {
            throw new RuntimeException("Unexpected reply: " + Tools.byteArrayToHexPrettyPrint(reply));
        }

        switch(reply[3] & 0b11000000) {
            case 0b00000000:
                return 22;
            case 0b01000000:
                return 17;
            case 0b10000000:
                return 12;
            case 0b11000000:
                return 10;
            default:
                throw new IllegalStateException("Unexpected transmit power value");
        }
    }

    public ModeSettings readModeSettings() throws SerialPortException {
        handle().writeBytes(new byte[]{(byte) 0xc1, (byte) 0x06, (byte) 0x01});
        byte[] reply = serialPort.readBytes(4);

        LOG.debug("readModeSettings() reply: {}", Tools.byteArrayToHexPrettyPrint(reply));

        if (reply.length != 4) {
            throw new RuntimeException("Unexpected reply: " + Tools.byteArrayToHexPrettyPrint(reply));
        }

        boolean rssiByteEnabled;
        switch(reply[3] & 0b00000001) {
            case 0b00000000:
                rssiByteEnabled = false;
                break;
            case 0b00000001:
                rssiByteEnabled = true;
                break;
            default:
                throw new IllegalStateException("Unexpected RSSI byte value");
        }

        TransmitMode transmitMode;
        switch(reply[3] & 0b00000010) {
            case 0b00000000:
                transmitMode = TransmitMode.TRANSPARENT;
                break;
            case 0b00000010:
                transmitMode = TransmitMode.P2P;
                break;
            default:
                throw new IllegalStateException("Unexpected transmit mode value");
        }

        return ModeSettings.create(rssiByteEnabled, transmitMode);
    }

    public void writeChannel(int channel) throws SerialPortException {
        handle().writeBytes(new byte[]{(byte) 0xc0, (byte) 0x05, (byte) 0x01, (byte) channel});
        serialPort.readBytes(4);
    }

    public void writeDefaultSpeeds() throws SerialPortException {
        handle().writeBytes(new byte[]{(byte) 0xc0, (byte) 0x03, (byte) 0x01, (byte) 0b00100011});
        serialPort.readBytes(4);
    }

    public void writeDefaultModes() throws SerialPortException {
        handle().writeBytes(new byte[]{(byte) 0xc0, (byte) 0x06, (byte) 0x01, (byte) 0b00000001});
        serialPort.readBytes(4);
    }

    public void writeDefaultTransmitPower() throws SerialPortException {
        handle().writeBytes(new byte[]{(byte) 0xc0, (byte) 0x04, (byte) 0x01, (byte) 0b00000000});
        serialPort.readBytes(4);
    }

    @Override
    public String getTypeDescription() {
        return "Waveshare SX126X LoRa HAT";
    }

    @Override
    public String getModeDescription() {
        if (!initialized) {
            return "not initialized";
        }

        String sb = "Channel " + channel + ", " +
                speeds.airSpeed() / 1000.0 + "K air speed, " +
                transmitPower + "dBm, " +
                modeSettings.transmitMode() + ", " +
                (modeSettings.rssiByteEnabled() ? "RSSI byte, " : "" ) +
                speeds.baudRate() + "bps UART, " +
                speeds.parityBit();

        return sb;
    }

    @Override
    public void transmit(String message) {

    }

    @Override
    public void onMessageReceived(Runnable runnable) {
        runnable.run();
    }

    @AutoValue
    public static abstract class Speeds {

        public abstract int airSpeed();
        public abstract ParityBit parityBit();
        public abstract int baudRate();

        public static Speeds create(int airSpeed, ParityBit parityBit, int baudRate) {
            return builder()
                    .airSpeed(airSpeed)
                    .parityBit(parityBit)
                    .baudRate(baudRate)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_SX126XLoRaHat_Speeds.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder airSpeed(int airSpeed);

            public abstract Builder parityBit(ParityBit parityBit);

            public abstract Builder baudRate(int baudRate);

            public abstract Speeds build();
        }
    }

    @AutoValue
    public static abstract class ModeSettings {

        public abstract boolean rssiByteEnabled();
        public abstract TransmitMode transmitMode();

        public static ModeSettings create(boolean rssiByteEnabled, TransmitMode transmitMode) {
            return builder()
                    .rssiByteEnabled(rssiByteEnabled)
                    .transmitMode(transmitMode)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_SX126XLoRaHat_ModeSettings.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder rssiByteEnabled(boolean rssiByteEnabled);

            public abstract Builder transmitMode(TransmitMode transmitMode);

            public abstract ModeSettings build();
        }

    }

}
