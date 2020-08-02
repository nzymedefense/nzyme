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

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.ConfigException;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.engine.ContactIdentifierProcess;
import horse.wtf.nzyme.bandits.trackers.devices.SX126XLoRaHat;
import horse.wtf.nzyme.bandits.trackers.devices.TrackerDevice;
import horse.wtf.nzyme.bandits.trackers.hid.TrackerHID;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.*;
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
    private StartTrackRequestMessageHandler startTrackRequestMessageHandler;
    private CancelTrackRequestMessageHandler cancelTrackRequestMessageHandler;
    private ContactStatusMessageHandler contactStatusMessageHandler;

    private List<TrackerMessage.StartTrackRequest> pendingStartBanditTrackRequests;
    private List<TrackerMessage.CancelTrackRequest> pendingCancelBanditTrackRequests;

    private final List<TrackerHID> hids;

    private final Counter rxCounter;
    private final Counter txCounter;
    private final Timer encryptionTimer;

    public GroundStation(Role nzymeRole,
                         String nzymeId,
                         String nzymeVersion,
                         MetricRegistry metrics,
                         ContactIdentifierProcess contacts,
                         @Nullable TrackerManager trackerManager,
                         TrackerDeviceConfiguration config) throws ConfigException {
        //noinspection UnstableApiUsage
        this.transmitQueue = new ArrayDeque(100);
        this.hids = Lists.newArrayList();

        this.rxCounter = metrics.counter(MetricNames.GROUNDSTATION_RX);
        this.txCounter = metrics.counter(MetricNames.GROUNDSTATION_TX);
        this.encryptionTimer = metrics.timer(MetricNames.GROUNDSTATION_ENCRYPTION_TIMING);
        metrics.register(MetricNames.GROUNDSTATION_QUEUE_SIZE, (Gauge<Integer>) transmitQueue::size);


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
            if (message.hasCancelTrackRequest()) {
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
            }

            // Contact Status.
            if (message.hasContactStatus()) {
                if (contactStatusMessageHandler != null) {
                    contactStatusMessageHandler.handle(message.getContactStatus());
                }
            }
        });

        // Send our pings.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("groundstation-pings-%d")
                .build())
                .scheduleAtFixedRate(() -> {
                    try {
                        String currentlyTrackedBandit = "";
                        if (nzymeRole == Role.TRACKER) {
                            Bandit tracked = contacts.getCurrentlyTrackedBandit();
                            currentlyTrackedBandit = tracked == null ? "" : tracked.uuid().toString();
                        }

                        transmit(TrackerMessage.Wrapper.newBuilder()
                                .setPing(TrackerMessage.Ping.newBuilder()
                                        .setSource(nzymeId)
                                        .setVersion(nzymeVersion)
                                        .setNodeType(TrackerMessage.Ping.NodeType.valueOf(nzymeRole.toString().toUpperCase()))
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
                            for (TrackerMessage.StartTrackRequest banditTrackRequest : Lists.newArrayList(pendingStartBanditTrackRequests)) {
                                LOG.debug("Sending start track request for [{}] to [{}]", banditTrackRequest.getUuid(), banditTrackRequest.getReceiver());

                                transmit(TrackerMessage.Wrapper.newBuilder().setStartTrackRequest(banditTrackRequest).build());
                            }

                            // Send outstanding cancel track requests.
                            for (TrackerMessage.CancelTrackRequest cancelTrackRequest : Lists.newArrayList(pendingCancelBanditTrackRequests)) {
                                LOG.debug("Sending cancel track requests to [{}].", cancelTrackRequest.getReceiver());

                                transmit(TrackerMessage.Wrapper.newBuilder().setCancelTrackRequest(cancelTrackRequest).build());
                            }

                            // Check if we can remove track requests.
                            if(trackerManager != null) {
                                List<TrackerMessage.StartTrackRequest> newStartTrackRequests = Lists.newArrayList();
                                List<TrackerMessage.CancelTrackRequest> newCancelTrackRequests = Lists.newArrayList();

                                for (TrackerMessage.StartTrackRequest outstandingRequest : new ArrayList<>(pendingStartBanditTrackRequests)) {
                                    Tracker tracker = trackerManager.getTrackers().get(outstandingRequest.getReceiver());

                                    if (tracker == null || TrackerManager.decideTrackerState(tracker).equals(TrackerState.DARK)) {
                                        LOG.info("Removing start bandit track request for [{}] from list of outstanding requests. Reason: Tracker has disappeared.",
                                                outstandingRequest.getReceiver());
                                        continue;
                                    }

                                    if (!outstandingRequest.getUuid().equals(tracker.getTrackingMode())) {
                                        // Tracker is still alive and not currently reporting as tracking the bandit from our request. Keep request outstanding.
                                        newStartTrackRequests.add(outstandingRequest);
                                    } else {
                                        LOG.info("Removing start bandit track request for [{}] from list of outstanding requests. Reason: Tracker has confirmed receipt.",
                                                outstandingRequest.getReceiver());
                                    }
                                }

                                for (TrackerMessage.CancelTrackRequest outstandingRequest : new ArrayList<>(pendingCancelBanditTrackRequests)) {
                                    Tracker tracker = trackerManager.getTrackers().get(outstandingRequest.getReceiver());

                                    if (tracker == null || TrackerManager.decideTrackerState(tracker).equals(TrackerState.DARK)) {
                                        LOG.info("Removing cancel bandit track request for [{}] from list of outstanding requests. Reason: Tracker has disappeared.",
                                                outstandingRequest.getReceiver());
                                        continue;
                                    }

                                    if (tracker.getTrackingMode() != null && !tracker.getTrackingMode().isEmpty()) {
                                        // Tracker is still alive and currently reporting as tracking a bandit. Keep request outstanding.
                                        newCancelTrackRequests.add(outstandingRequest);
                                    } else {
                                        LOG.info("Removing cancel bandit track request for [{}] from list of outstanding requests. Reason: Tracker has confirmed receipt.",
                                                outstandingRequest.getReceiver());
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
        if (transmitQueue.size() > 5) {
            LOG.warn("Transmit queue size is unusually large at <{}> entries.", transmitQueue.size());
        }

        transmitQueue.addLast(message.toByteArray());
    }

    public void startTrackRequest(TrackerMessage.StartTrackRequest newRequest) {
        for (TrackerMessage.StartTrackRequest request : Lists.newArrayList(pendingStartBanditTrackRequests)) {
            if (request.getReceiver().equals(newRequest.getReceiver()) && request.getUuid().equals(newRequest.getUuid())) {
                // Same request already in queue.
                return;
            }
        }

        LOG.info("Sending START tracking request to tracker [{}] for bandit [{}].", newRequest.getReceiver(), newRequest.getUuid());
        pendingStartBanditTrackRequests.add(newRequest);
    }

    public void cancelTrackRequest(TrackerMessage.CancelTrackRequest cancelRequest) {
        // Remove all pending start track requests from the queue.
        List<TrackerMessage.StartTrackRequest> newRequests = Lists.newArrayList();
        for (TrackerMessage.StartTrackRequest request : Lists.newArrayList(pendingStartBanditTrackRequests)) {
            if (!request.getReceiver().equals(cancelRequest.getReceiver())) {
                newRequests.add(request);
            }
        }
        this.pendingStartBanditTrackRequests = newRequests;

        LOG.info("Sending STOP tracking request to tracker [{}].", cancelRequest.getReceiver());
        this.pendingCancelBanditTrackRequests.add(cancelRequest);
    }

    public void onPingReceived(PingMessageHandler pingHandler) {
        this.pingHandler = pingHandler;
    }

    public void onStartTrackRequestReceived(StartTrackRequestMessageHandler startTrackRequestMessageHandler) {
        this.startTrackRequestMessageHandler = startTrackRequestMessageHandler;
    }

    public void onCancelTrackRequestReceived(CancelTrackRequestMessageHandler cancelTrackRequestMessageHandler) {
        this.cancelTrackRequestMessageHandler = cancelTrackRequestMessageHandler;
    }

    public void onContactStatusReceived(ContactStatusMessageHandler contactStatusMessageHandler) {
        this.contactStatusMessageHandler = contactStatusMessageHandler;
    }

    public void handleTrackerConnectionStateChange(List<TrackerState> states) {
        for (TrackerHID hid : hids) {
            hid.onConnectionStateChange(states);
        }

    }

    public void registerHID(TrackerHID hid) {
        hids.add(hid);
    }

    public boolean trackerHasPendingStartTrackingRequest(String trackerName) {
        for (TrackerMessage.StartTrackRequest outstandingRequest : new ArrayList<>(pendingStartBanditTrackRequests)) {
            if (outstandingRequest.getReceiver().equals(trackerName)) {
                return true;
            }
        }

        return false;
    }

    public boolean trackerHasPendingCancelTrackingRequest(String trackerName) {
        for (TrackerMessage.CancelTrackRequest outstandingRequest : new ArrayList<>(pendingCancelBanditTrackRequests)) {
            if (outstandingRequest.getReceiver().equals(trackerName)) {
                return true;
            }
        }

        return false;
    }

    public boolean trackerHasPendingAnyTrackingRequest(String trackerName) {
        return trackerHasPendingStartTrackingRequest(trackerName) || trackerHasPendingCancelTrackingRequest(trackerName);
    }

}
