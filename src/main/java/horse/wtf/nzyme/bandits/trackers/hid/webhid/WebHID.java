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

package horse.wtf.nzyme.bandits.trackers.hid.webhid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.auto.value.AutoValue;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeTracker;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.trackers.TrackerState;
import horse.wtf.nzyme.bandits.trackers.hid.TrackerHID;
import horse.wtf.nzyme.bandits.trackers.hid.webhid.rest.NzymeTrackerInjectionBinder;
import horse.wtf.nzyme.bandits.trackers.hid.webhid.rest.resources.TrackerWebHIDAssetsResource;
import horse.wtf.nzyme.bandits.trackers.hid.webhid.rest.resources.TrackerWebHIDResource;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.ChannelDesignator;
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.rest.CORSFilter;
import horse.wtf.nzyme.rest.NzymeExceptionMapper;
import horse.wtf.nzyme.rest.ObjectMapperProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WebHID implements TrackerHID {

    private static final Logger LOG = LogManager.getLogger(WebHID.class);

    private final NzymeTracker nzyme;

    // State information.
    private int leaderRSSI = 0;
    private boolean trackerDeviceLive = false;
    private boolean allProbesLive = false;
    private ChannelDesignator.DESIGNATION_STATUS channelDesignationStatus = ChannelDesignator.DESIGNATION_STATUS.UNLOCKED;

    @SuppressWarnings("UnstableApiUsage")
    private final EvictingQueue<WebHID.Event> events;

    public WebHID(NzymeTracker nzyme) {
        this.nzyme = nzyme;
        this.events = EvictingQueue.create(20);
    }

    @Override
    public void initialize() {
        // Spin up REST API and web interface.
        java.util.logging.Logger.getLogger("org.glassfish.grizzly").setLevel(Level.SEVERE);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new NzymeTrackerInjectionBinder(this.nzyme, this));
        resourceConfig.register(new CORSFilter());
        resourceConfig.register(new ObjectMapperProvider());
        resourceConfig.register(new JacksonJaxbJsonProvider());
        resourceConfig.register(new NzymeExceptionMapper());

        // Register REST API resources.
        resourceConfig.register(TrackerWebHIDResource.class);

        // Enable GZIP.
        resourceConfig.registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);

        // Register web interface asset resources.
        resourceConfig.register(TrackerWebHIDAssetsResource.class);

        URI listenURI = URI.create("http://0.0.0.0:13000"); // TODO make configurable
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(listenURI, resourceConfig);

        // Start server.
        try {
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start REST API.", e);
        }

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("webhid-secup-%d").build())
                .scheduleWithFixedDelay(() -> {
                    allProbesLive = true;
                    Dot11MonitorProbe firstMonitor = null;
                    for (Dot11Probe probe : nzyme.getProbes()) {
                        if (!probe.isInLoop() || !probe.isActive()) {
                            allProbesLive = false;
                            break;
                        }
                    }
                    for (Dot11Probe probe : nzyme.getProbes()) {
                        if (probe instanceof Dot11MonitorProbe) {
                            firstMonitor = (Dot11MonitorProbe) probe;
                            break;
                        }
                    }

                    if (firstMonitor != null) {
                        channelDesignationStatus = firstMonitor.getChannelDesignator().getStatus();
                    }

                    trackerDeviceLive = nzyme.getGroundStation().getTrackerDevice().isHealthy();
                }, 0, 1, TimeUnit.SECONDS);

        LOG.info("Started WebHID and REST API at [{}].", listenURI);
    }

    @Override
    public void onConnectionStateChange(List<TrackerState> connectionState) {

    }

    @Override
    public void onPingFromLeaderReceived(TrackerMessage.Ping ping, int rssi) {
        this.leaderRSSI = rssi;
    }

    @Override
    public void onPingFromTrackerReceived(TrackerMessage.Ping ping, int rssi) {

    }

    @Override
    public void onStartTrackingRequestReceived(TrackerMessage.StartTrackRequest request) {
        event(request.getSource(), "Received request to track bandit.");
    }

    @Override
    public void onCancelTrackingRequestReceived(TrackerMessage.CancelTrackRequest request) {
        event(request.getSource(), "All bandit tracking canceled on leader request.");
    }

    @Override
    public void onInitialContactWithTrackedBandit(Bandit bandit) {

    }

    @Override
    public void onBanditTrace(Bandit bandit, int rssi) {

    }

    @Override
    public void onChannelSwitch(int previousChannel, int newChannel) {

    }

    private void event(String source, String message) {
        //noinspection UnstableApiUsage
        events.add(WebHID.Event.create(DateTime.now(), source, message));
    }

    public List<WebHID.Event> getEvents() {
        return ImmutableList.copyOf(events);
    }

    public int getLeaderRSSI() {
        return leaderRSSI;
    }

    public boolean isAllProbesLive() {
        return allProbesLive;
    }

    public boolean isTrackerDeviceLive() {
        return trackerDeviceLive;
    }

    public List<Integer> allMonitorChannels() {
        List<Integer> channels = Lists.newArrayList();
        for (Dot11Probe probe : nzyme.getProbes()) {
            channels.add(probe.getCurrentChannel());
        }

        return channels;
    }

    public ChannelDesignator.DESIGNATION_STATUS getChannelDesignationStatus() {
        return channelDesignationStatus;
    }

    @AutoValue
    public static abstract class Event {

        @JsonProperty
        public abstract DateTime timestamp();

        @JsonProperty
        public abstract String source();

        @JsonProperty
        public abstract String message();

        public static WebHID.Event create(DateTime timestamp, String source, String message) {
            return builder()
                    .timestamp(timestamp)
                    .source(source)
                    .message(message)
                    .build();
        }

        public static WebHID.Event.Builder builder() {
            return new AutoValue_WebHID_Event.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract WebHID.Event.Builder timestamp(DateTime timestamp);

            public abstract WebHID.Event.Builder source(String source);

            public abstract WebHID.Event.Builder message(String message);

            public abstract WebHID.Event build();
        }

    }

}
