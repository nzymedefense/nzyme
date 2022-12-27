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

package app.nzyme.core.periodicals.alerting.tracks;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.alerts.MultipleTrackAlert;
import app.nzyme.core.configuration.Dot11BSSIDDefinition;
import app.nzyme.core.configuration.Dot11NetworkDefinition;
import app.nzyme.core.dot11.networks.BSSID;
import app.nzyme.core.dot11.networks.Channel;
import app.nzyme.core.dot11.networks.SSID;
import app.nzyme.core.dot11.networks.signalstrength.tracks.SignalWaterfallHistogramLoader;
import app.nzyme.core.dot11.networks.signalstrength.tracks.Track;
import app.nzyme.core.dot11.networks.signalstrength.tracks.TrackDetector;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;

public class SignalTrackMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(SignalTrackMonitor.class);

    private final NzymeLeader nzyme;

    private final Timer timer;

    public SignalTrackMonitor(NzymeLeader nzyme) {
        this.nzyme = nzyme;

        this.timer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.SIGNAL_TRACK_MONITOR_TIMING));
    }


    @Override
    protected void execute() {
        Timer.Context ctx = this.timer.time();

        try {
            for (BSSID bssid : nzyme.getNetworks().getBSSIDs().values()) {
                for (SSID ssid : bssid.ssids().values()) {
                    if (!ssid.isHumanReadable()) {
                        continue;
                    }

                    // Only run for our own networks.
                    if (!nzyme.getConfiguration().ourSSIDs().contains(ssid.name())) {
                        continue;
                    }

                    Dot11NetworkDefinition network = nzyme.getConfiguration().findNetworkDefinition(bssid.bssid(), ssid.name());
                    if (network == null) {
                        continue;
                    }

                    Dot11BSSIDDefinition config = nzyme.getConfiguration().findBSSIDDefinition(bssid.bssid(), ssid.name());
                    TrackDetector.TrackDetectorConfig tdc = (config == null || config.trackDetectorConfig() == null) ? TrackDetector.DEFAULT_CONFIG : config.trackDetectorConfig();

                    SignalWaterfallHistogramLoader signalWaterfallHistogramLoader = new SignalWaterfallHistogramLoader(nzyme);
                    for (Channel channel : ssid.channels().values()) {
                        TrackDetector trackDetector = new TrackDetector(signalWaterfallHistogramLoader.load(bssid, ssid, channel, 60*15));
                        List<Track> tracks = trackDetector.detect(tdc);

                        if (tracks.size() > 1) {
                            nzyme.getAlertsService().handle(MultipleTrackAlert.create(
                                    DateTime.now(),
                                    ssid.name(),
                                    bssid.bssid(),
                                    channel.channelNumber(),
                                    tracks.size())
                            );
                        }
                    }
                }
            }
        } catch(Exception e) {
            LOG.error("Signal Track Monitor run failed.", e);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public String getName() {
        return "SignalTrackMonitor";
    }

}
