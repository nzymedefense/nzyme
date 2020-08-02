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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.protobuf.InvalidProtocolBufferException;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.WrapperMessageHandler;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.security.transport.TransportEncryption;
import horse.wtf.nzyme.util.Tools;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SX126XLoRaHat implements TrackerDevice {

    private static final Logger LOG = LogManager.getLogger(SX126XLoRaHat.class);
    private static final int BAUD = 2400;

    private static final short NULL_BYTE_SEQUENCE_COUNT = 3;

    private final String portName;

    private SerialPort serialPort;
    private WrapperMessageHandler messageHandler = null;

    TransportEncryption encryption;

    private final Counter rxCounter;
    private final Counter txCounter;
    private final Timer encryptionTimer;

    public SX126XLoRaHat(String portName, String encryptionKey, Counter rxCounter, Counter txCounter, Timer encryptionTimer) {
        this.portName = portName;

        this.rxCounter = rxCounter;
        this.txCounter = txCounter;
        this.encryptionTimer = encryptionTimer;

        try {
            this.encryption = new TransportEncryption(encryptionKey);
        } catch(Exception e) {
            throw new RuntimeException("Could not initialize transport encryption.", e);
        }
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
        short chunkByteCount = 0;

        while(true) {
            try {
                byte[] b = handle().readBytes(1);

                if (b == null) {
                    // Nothing to read. End of message.
                    nulCount = 0;
                    buffer.reset();
                    continue;
                }

                chunkByteCount++;
                if(chunkByteCount == 241) {
                    // This is the first byte of a new chunk and it's the RSSI byte again, messing up our payload. Ignore it.
                    chunkByteCount = 0;
                    continue;
                }

                if (b[0] == 0x00) {
                    // Our frames end with NULL bytes.
                    nulCount++;

                    if (nulCount == NULL_BYTE_SEQUENCE_COUNT) {
                        // Message is complete!
                        try {
                            byte[] message = buffer.toByteArray();
                            byte[] rssiChunk = handle().readBytes(1);

                            if(rssiChunk == null || rssiChunk.length != 1) {
                                continue;
                            }

                            int rssi = rssiChunk[0] & 0xFF;
                            LOG.debug("Received <{}> bytes: {}", message.length, Tools.byteArrayToHexPrettyPrint(message));
                            byte[] decrypted = encryption.decrypt(message);
                            messageHandler.handle(
                                    TrackerMessage.Wrapper.parseFrom(decrypted),
                                    rssi
                            );

                            rxCounter.inc(decrypted.length);
                        } catch (javax.crypto.AEADBadTagException e) {
                            LOG.debug("Skipping invalid message. Payload was: [{}]", Tools.byteArrayToHexPrettyPrint(buffer.toByteArray()), e);
                            continue;
                        } catch(InvalidProtocolBufferException e) {
                            LOG.debug("Skipping invalid protobuf message. Payload was: [{}]", Tools.byteArrayToHexPrettyPrint(buffer.toByteArray()), e);
                            continue;
                        } finally {
                            nulCount = 0;
                            chunkByteCount = 0;
                            buffer.reset();
                        }
                    }

                    continue;
                }

                if (nulCount > 0) {
                    for(int i = 0; i < nulCount; i++) {
                        buffer.write(0x00);
                    }

                    nulCount = 0;
                }

                try {
                    buffer.write(b);
                } catch (IOException e) {
                    LOG.warn("Could not write to buffer.", e);
                    buffer.reset();
                    nulCount = 0;
                    chunkByteCount = 0;
                    continue;
                }
            } catch(Exception e) {
                LOG.warn("Error in read loop.", e);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) { }
            }
        }
    }

    private SerialPort handle() throws SerialPortException {
        if (serialPort == null || !serialPort.isOpened()) {
            serialPort = new SerialPort(this.portName);
            serialPort.openPort();
            serialPort.setParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        }

        return serialPort;
    }

    @Override
    public String getTypeDescription() {
        return "Waveshare SX126X LoRa HAT";
    }

    @Override
    public synchronized void transmit(byte[] message) {
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();

            Timer.Context time = encryptionTimer.time();
            byte[] encrypted = encryption.encrypt(message);
            time.stop();

            LOG.debug("Sending payload: {}", Tools.byteArrayToHexPrettyPrint(encrypted));

            payload.write(encrypted);

            for (short i = 0; i < NULL_BYTE_SEQUENCE_COUNT; i++) {
                payload.write(0x00);
            }
            byte[] buf = payload.toByteArray();

            LOG.debug("Transmitting <{}> bytes: {}", buf.length, Tools.byteArrayToHexPrettyPrint(buf));

            handle().writeBytes(buf);
            txCounter.inc(buf.length);

            // Spread out message sending to not overload LoRa bands or UART connection buffer.
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        } catch (Exception e) {
            LOG.error("Could not transmit message.", e);
        }
    }

    @Override
    public void onMessageReceived(WrapperMessageHandler receiver) {
        this.messageHandler = receiver;
    }

}
