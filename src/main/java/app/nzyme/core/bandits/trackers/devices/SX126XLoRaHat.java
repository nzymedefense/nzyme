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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.google.protobuf.InvalidProtocolBufferException;
import app.nzyme.core.bandits.trackers.messagehandlers.WrapperMessageHandler;
import app.nzyme.core.bandits.trackers.protobuf.TrackerMessage;
import app.nzyme.core.security.transport.TransportEncryption;
import app.nzyme.core.util.Tools;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    private boolean healthyReceive = false;
    private boolean healthyTransmit = false;

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
    public boolean isHealthy() {
        return this.serialPort != null && this.serialPort.isOpened() && healthyTransmit && healthyReceive;
    }

    @Override
    public void readLoop() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        short nulCount = 0;
        short chunkByteCount = 0;

        while(true) {
            try {
                byte[] b = handle().readBytes(1);
                healthyReceive = true;

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
                healthyReceive = false;

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

            healthyTransmit = true;

            // Spread out message sending to not overload LoRa bands or UART connection buffer.
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
        } catch (Exception e) {
            LOG.error("Could not transmit message.", e);
            healthyTransmit = false;
        }
    }

    @Override
    public void onMessageReceived(WrapperMessageHandler receiver) {
        this.messageHandler = receiver;
    }

}
