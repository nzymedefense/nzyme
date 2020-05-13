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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
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
import horse.wtf.nzyme.bandits.trackers.messagehandlers.CancelTrackRequestMessageHandler;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.PingMessageHandler;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.StartTrackRequestMessageHandler;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.configuration.ConfigurationKeys;
import horse.wtf.nzyme.configuration.TrackerDeviceConfiguration;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GroundStation implements Runnable {

    private static final Logger LOG = LogManager.getLogger(GroundStation.class);

    private final TrackerDevice trackerDevice;

    private final Deque<byte[]> transmitQueue;

    private PingMessageHandler pingHandler;
    private BanditBroadcastMessageHandler banditBroadcastHandler;
    private StartTrackRequestMessageHandler startTrackRequestMessageHandler;
    private CancelTrackRequestMessageHandler cancelTrackRequestMessageHandler;

    private List<BanditTrackRequest> pendingStartBanditTrackRequests;
    private List<BanditTrackRequest> pendingCancelBanditTrackRequests;

    private final List<TrackerHID> hids;

    private final Counter rxCounter;
    private final Counter txCounter;
    private final Timer encryptionTimer;

    public GroundStation(Role nzymeRole,
                         String nzymeId,
                         String nzymeVersion,
                         MetricRegistry metrics,
                         BanditListProvider bandits,
                         @Nullable TrackerManager trackerManager,
                         TrackerDeviceConfiguration config) throws ConfigException {
        //noinspection UnstableApiUsage
        this.transmitQueue = new ArrayDeque(100);
        this.hids = Lists.newArrayList();

        this.rxCounter = metrics.counter(MetricNames.GROUNDSTATION_RX);
        this.txCounter = metrics.counter(MetricNames.GROUNDSTATION_TX);
        this.encryptionTimer = metrics.timer(MetricNames.GROUNDSTATION_ENCRYPTION_TIMING);

        this.pendingStartBanditTrackRequests = Lists.newArrayList();
        this.pendingCancelBanditTrackRequests = Lists.newArrayList();

        TrackerDevice.TYPE deviceType;
        try {
            deviceType = TrackerDevice.TYPE.valueOf(config.type());
        } catch(IllegalArgumentException e) {
            throw new ConfigException.BadValue(ConfigurationKeys.TRACKER_DEVICE + "." + ConfigurationKeys.TYPE,
                    "Invalid tracker device type.", e);
        }

        switch (deviceType) {
            case SX126X_LORA:
                this.trackerDevice = new SX126XLoRaHat(
                        config.parameters().getString(ConfigurationKeys.SERIAL_PORT),
                        config.parameters().getString(ConfigurationKeys.ENCRYPTION_KEY),
                        rxCounter,
                        txCounter,
                        encryptionTimer
                );
                break;
            default:
                throw new IllegalStateException("Unexpected device type: " + deviceType);
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

            // Start tracking request.
            if (message.hasStartTrackRequest()) {
                TrackerMessage.StartTrackRequest request = message.getStartTrackRequest();
                if (!request.getReceiver().equals(nzymeId)) {
                    LOG.debug("Ignoring start tracking request for other tracker [{}].", request.getReceiver());
                    return;
                }

                if (startTrackRequestMessageHandler != null) {
                    startTrackRequestMessageHandler.handle(message.getStartTrackRequest());
                }

                for (TrackerHID hid : hids) {
                    hid.onStartTrackingRequestReceived(message.getStartTrackRequest());
                }
            }

            // Cancel tracking request.
            /*if (message.hasCancelTrackRequest()) {
                TrackerMessage.CancelTrackRequest request = message.getCancelTrackRequest();
                if (!request.getReceiver().equals(nzymeId)) {
                    LOG.debug("Ignoring cancel tracking request for other tracker [{}].", request.getReceiver());
                    return;
                }

                if (cancelTrackRequestMessageHandler != null) {
                    cancelTrackRequestMessageHandler.handle(message.getCancelTrackRequest());
                }

                for (TrackerHID hid : hids) {
                    hid.onCancelTrackingRequestReceived(message.getCancelTrackRequest());
                }
            }*/ // TODO: delete no longer pending cancel track requests like the start requests
        });

        // Send our pings.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("groundstation-pings-%d")
                .build())
                .scheduleAtFixedRate(() -> {
                    try {
                        List<Bandit> banditList = bandits.getBanditList();

                        String currentlyTrackedBandit = "";
                        if (nzymeRole == Role.TRACKER) {
                            Bandit tracked = bandits.getCurrentlyTrackedBandit();
                            currentlyTrackedBandit = tracked == null ? "" : tracked.uuid().toString();
                        }

                        transmit(TrackerMessage.Wrapper.newBuilder()
                                .setPing(TrackerMessage.Ping.newBuilder()
                                        .setSource(nzymeId)
                                        .setVersion(nzymeVersion)
                                        .setNodeType(TrackerMessage.Ping.NodeType.valueOf(nzymeRole.toString().toUpperCase()))
                                        .setBanditHash(BanditHashCalculator.calculate(banditList))
                                        .setBanditCount(banditList.size())
                                        .setTrackingMode(currentlyTrackedBandit)
                                        .setTimestamp(DateTime.now().getMillis())
                                        .build())
                                .build());
                    } catch(Exception e) {
                        LOG.error("Could not send Ground Station ping.", e);
                    }
                }, 0, 5, TimeUnit.SECONDS);

        // Bandit track request loop.
        // We cannot assume that a track request is received by a tracker, so we have to keep on sending them
        // until a tracker confirms the tracking status in a ping.
        if (nzymeRole == Role.LEADER) {
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("groundstation-track-requests-%d")
                    .build())
                    .scheduleAtFixedRate(() -> {
                        try {
                            // Send outstanding start track requests.
                            for (BanditTrackRequest banditTrackRequest : Lists.newArrayList(pendingStartBanditTrackRequests)) {
                                LOG.debug("Sending start track request for [{}] to [{}]", banditTrackRequest.banditUUID(), banditTrackRequest.trackerName());

                                transmit(TrackerMessage.Wrapper.newBuilder()
                                        .setStartTrackRequest(TrackerMessage.StartTrackRequest.newBuilder()
                                                .setSource(nzymeId)
                                                .setReceiver(banditTrackRequest.trackerName())
                                                .setUuid(banditTrackRequest.banditUUID().toString())
                                                .build()
                                        ).build()
                                );
                            }

                            // Send outstanding cancel track requests.
                            for (BanditTrackRequest banditTrackRequest : Lists.newArrayList(pendingCancelBanditTrackRequests)) {
                                LOG.debug("Sending cancel track requests to [{}].", banditTrackRequest.trackerName());

                                transmit(TrackerMessage.Wrapper.newBuilder()
                                        .setCancelTrackRequest(TrackerMessage.CancelTrackRequest.newBuilder()
                                                .setSource(nzymeId)
                                                .setReceiver(banditTrackRequest.trackerName())
                                                .build()
                                        ).build()
                                );
                            }

                            // Check if we can remove track requests.
                            if(trackerManager != null) {
                                List<BanditTrackRequest> newStartTrackRequests = Lists.newArrayList();
                                List<BanditTrackRequest> newCancelTrackRequests = Lists.newArrayList();

                                for (BanditTrackRequest outstandingRequest : new ArrayList<>(pendingStartBanditTrackRequests)) {
                                    Tracker tracker = trackerManager.getTrackers().get(outstandingRequest.trackerName());

                                    if (tracker == null) {
                                        LOG.info("Removing bandit track request [{}] from list of outstanding requests. Reason: Tracker has disappeared.",
                                                outstandingRequest);
                                    }

                                    if (!outstandingRequest.banditUUID().toString().equals(tracker.getTrackingMode())) {
                                        // Tracker is still alive and not currently reporting as tracking the bandit from our request. Keep request outstanding.
                                        newStartTrackRequests.add(outstandingRequest);
                                    } else {
                                        LOG.info("Removing bandit track request [{}] from list of outstanding requests. Reason: Tracker has confirmed receipt.",
                                                outstandingRequest);
                                    }
                                }


                                pendingStartBanditTrackRequests = newStartTrackRequests;
                                pendingCancelBanditTrackRequests = newCancelTrackRequests;
                            }
                        } catch (Exception e) {
                            LOG.error("Could not send track request.", e);
                        }
                    }, 0, 5, TimeUnit.SECONDS);
        }

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
        if (transmitQueue.size() > 50) {
            LOG.warn("Transmit queue size is unusually large at <{}> entries.", transmitQueue.size());
        }

        if (message.hasBanditBroadcast()) {
            // Bandit broadcasts have least priority.
            transmitQueue.addLast(message.toByteArray());
        } else {
            transmitQueue.addFirst(message.toByteArray());
        }
    }

    public void startTrackRequest(String trackerName, UUID banditUUID) {
        for (BanditTrackRequest request : Lists.newArrayList(pendingStartBanditTrackRequests)) {
            if (request.trackerName().equals(trackerName) && request.banditUUID().equals(banditUUID)) {
                // Same request already in queue.
                return;
            }
        }

        LOG.info("Sending START tracking request to tracker [{}] for bandit [{}].", trackerName, banditUUID);

        pendingStartBanditTrackRequests.add(BanditTrackRequest.create(trackerName, banditUUID));
    }

    public void cancelTrackRequest(String trackerName, UUID banditUUID) {
        // Remove all pending start track requests from the queue.
        List<BanditTrackRequest> newRequests = Lists.newArrayList();
        for (BanditTrackRequest request : Lists.newArrayList(pendingStartBanditTrackRequests)) {
            if (!request.trackerName().equals(trackerName) && request.banditUUID().equals(banditUUID)) {
                newRequests.add(request);
            }
        }
        this.pendingStartBanditTrackRequests = newRequests;

        LOG.info("Sending STOP tracking request to tracker [{}] for bandit [{}].", trackerName, banditUUID);

        // Add cancel request.
        this.pendingCancelBanditTrackRequests.add(BanditTrackRequest.create(trackerName, banditUUID));
    }

    public void onPingReceived(PingMessageHandler pingHandler) {
        this.pingHandler = pingHandler;
    }

    public void onBanditBroadcastReceived(BanditBroadcastMessageHandler banditBroadcastHandler) {
        this.banditBroadcastHandler = banditBroadcastHandler;
    }

    public void onStartTrackRequestReceived(StartTrackRequestMessageHandler startTrackRequestMessageHandler) {
        this.startTrackRequestMessageHandler = startTrackRequestMessageHandler;
    }

    public void onCancelTrackRequestReceived(CancelTrackRequestMessageHandler cancelTrackRequestMessageHandler) {
        this.cancelTrackRequestMessageHandler = cancelTrackRequestMessageHandler;
    }

    public void handleTrackerConnectionStateChange(List<TrackerState> states) {
        for (TrackerHID hid : hids) {
            hid.onConnectionStateChange(states);
        }

    }

    public void registerHID(TrackerHID hid) {
        hids.add(hid);
    }

    public boolean trackerHasPendingTrackingRequest(String trackerName) {
        for (BanditTrackRequest outstandingRequest : new ArrayList<>(pendingStartBanditTrackRequests)) {
            if (outstandingRequest.trackerName().equals(trackerName)) {
                return true;
            }
        }

        for (BanditTrackRequest outstandingRequest : new ArrayList<>(pendingCancelBanditTrackRequests)) {
            if (outstandingRequest.trackerName().equals(trackerName)) {
                return true;
            }
        }

        return false;
    }

}
