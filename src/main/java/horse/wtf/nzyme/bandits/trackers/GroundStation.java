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

package horse.wtf.nzyme.bandits.trackers;

import com.google.common.collect.EvictingQueue;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.ConfigException;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.bandits.trackers.devices.SX126XLoRaHat;
import horse.wtf.nzyme.bandits.trackers.devices.TrackerDevice;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.PingMessageHandler;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.configuration.ConfigurationKeys;
import horse.wtf.nzyme.configuration.TrackerDeviceConfiguration;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GroundStation implements Runnable {

    private static final Logger LOG = LogManager.getLogger(GroundStation.class);

    private final TrackerDevice trackerDevice;

    private final Queue<byte[]> transmitQueue;

    private PingMessageHandler pingHandler;

    public GroundStation(Role nzymeRole, String nzymeId, String nzymeVersion, TrackerDeviceConfiguration config) throws ConfigException {
        //noinspection UnstableApiUsage
        this.transmitQueue = EvictingQueue.create(100);

        TrackerDevice.TYPE deviceType;
        try {
            deviceType = TrackerDevice.TYPE.valueOf(config.type());
        } catch(IllegalArgumentException e) {
            throw new ConfigException.BadValue(ConfigurationKeys.TRACKER_DEVICE + "." + ConfigurationKeys.TYPE,
                    "Invalid tracker device type.", e);
        }

        switch (deviceType) {
            case SX126X_LORA:
                this.trackerDevice = new SX126XLoRaHat(config.parameters().getString(ConfigurationKeys.SERIAL_PORT));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + deviceType);
        }

        // Handle incoming messages.
        this.trackerDevice.onMessageReceived(message -> {
            if (message.hasPing()) {
                if (pingHandler != null) {
                    pingHandler.handle(message.getPing());
                }
            }

            // TODO implement all types
        });

        // Send our pings.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("groundstation-pings-%d")
                .build())
                .scheduleAtFixedRate(() -> transmit(TrackerMessage.Wrapper.newBuilder()
                        .setPing(TrackerMessage.Ping.newBuilder()
                                .setSource(nzymeId)
                                .setVersion(nzymeVersion)
                                .setNodeType(TrackerMessage.Ping.NodeType.valueOf(nzymeRole.toString().toUpperCase()))
                                .setTimestamp(DateTime.now().getMillis())
                                .build())
                        .build()), 0, 5, TimeUnit.SECONDS);

        // Listen for messages.
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("groundstation-listener-%d")
                .build())
                .submit(trackerDevice::readLoop);

        LOG.info("Ground Station is online.");
    }

    @Override
    public void run() {
        while(true) {
            try {
                while(transmitQueue.peek() != null) {
                    trackerDevice.transmit(transmitQueue.poll());
                }
            } catch (SerialPortException e) {
                LOG.error("Could not transmit message to trackers.", e);
            }

            try {
                Thread.sleep(25); }
            catch (InterruptedException ignored) { }
        }
    }

    public void stop() {
        trackerDevice.stop();
    }

    public void transmit(@NotNull TrackerMessage.Wrapper message) {
        transmitQueue.add(message.toByteArray());
    }

    public void onPingReceived(PingMessageHandler pingHandler) {
        this.pingHandler = pingHandler;
    }

}
