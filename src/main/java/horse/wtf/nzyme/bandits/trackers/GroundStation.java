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
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.ConfigException;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.BanditHashCalculator;
import horse.wtf.nzyme.bandits.BanditListProvider;
import horse.wtf.nzyme.bandits.trackers.devices.SX126XLoRaHat;
import horse.wtf.nzyme.bandits.trackers.devices.TrackerDevice;
import horse.wtf.nzyme.bandits.trackers.hid.TrackerHID;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.BanditBroadcastMessageHandler;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.PingMessageHandler;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.configuration.ConfigurationKeys;
import horse.wtf.nzyme.configuration.TrackerDeviceConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GroundStation implements Runnable {

    private static final Logger LOG = LogManager.getLogger(GroundStation.class);

    private final TrackerDevice trackerDevice;

    private final Queue<byte[]> transmitQueue;

    private PingMessageHandler pingHandler;
    private BanditBroadcastMessageHandler banditBroadcastHandler;

    private final List<TrackerHID> hids;

    public GroundStation(Role nzymeRole, String nzymeId, String nzymeVersion, BanditListProvider bandits, TrackerDeviceConfiguration config) throws ConfigException {
        //noinspection UnstableApiUsage
        this.transmitQueue = EvictingQueue.create(100);
        this.hids = Lists.newArrayList();

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
        this.trackerDevice.onMessageReceived((message, rssi) -> {
            // Ping.
            if (message.hasPing()) {
                if (pingHandler != null) {
                    pingHandler.handle(message.getPing(), rssi);
                }

                for (TrackerHID hid : hids) {
                    switch (message.getPing().getNodeType()) {
                        case LEADER:
                            hid.onPingFromLeaderReceived(message.getPing(), rssi);
                            break;
                        case TRACKER:
                            hid.onPingFromTrackerReceived(message.getPing(), rssi);
                            break;
                        case UNRECOGNIZED:
                        case DRONE:
                            // Currently not handled.
                            break;
                    }
                }
            }

            // Bandit broadcast.
            if (message.hasBanditBroadcast()) {
                if (banditBroadcastHandler != null) {
                    banditBroadcastHandler.handle(message.getBanditBroadcast());
                }

                for (TrackerHID hid : hids) {
                    hid.onBanditReceived(message.getBanditBroadcast());
                }
            }

            // TODO implement all types
        });

        // Send our pings.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("groundstation-pings-%d")
                .build())
                .scheduleAtFixedRate(() -> {
                    try {
                        List<Bandit> banditList = bandits.getBanditList();

                        transmit(TrackerMessage.Wrapper.newBuilder()
                                .setPing(TrackerMessage.Ping.newBuilder()
                                        .setSource(nzymeId)
                                        .setVersion(nzymeVersion)
                                        .setNodeType(TrackerMessage.Ping.NodeType.valueOf(nzymeRole.toString().toUpperCase()))
                                        .setBanditHash(BanditHashCalculator.calculate(banditList))
                                        .setBanditCount(banditList.size())
                                        .setTimestamp(DateTime.now().getMillis())
                                        .build())
                                .build());
                    } catch(Exception e) {
                        LOG.error("Could not send Ground Station ping.", e);
                    }
                }, 0, 5, TimeUnit.SECONDS);

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
                    byte[] msg = transmitQueue.poll();
                    trackerDevice.transmit(msg);
                }
            } catch (Exception e) {
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

    public void onBanditBroadcastReceived(BanditBroadcastMessageHandler banditBroadcastHandler) {
        this.banditBroadcastHandler = banditBroadcastHandler;
    }

    public void registerHID(TrackerHID hid) {
        hids.add(hid);
    }

}
