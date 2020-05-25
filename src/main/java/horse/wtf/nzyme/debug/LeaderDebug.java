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

package horse.wtf.nzyme.debug;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.bandits.BanditHashCalculator;
import horse.wtf.nzyme.bandits.trackers.Tracker;
import horse.wtf.nzyme.bandits.trackers.TrackerManager;
import horse.wtf.nzyme.configuration.ConfigurationKeys;
import horse.wtf.nzyme.debug.trackers.SignalStrengthLink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LeaderDebug {

    private static final Logger LOG = LogManager.getLogger(LeaderDebug.class);

    private final NzymeLeader nzyme;

    public LeaderDebug(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        Config debugConfig = nzyme.getConfiguration().debugConfig();

        if (debugConfig == null) {
            return;
        }

        // Ground Station: Signal Strength Reporter.
        if (debugConfig.hasPath(ConfigurationKeys.SIGNAL_LINK)) {
            Config signalLinkConfig = debugConfig.getConfig(ConfigurationKeys.SIGNAL_LINK);

            if (nzyme.getGroundStation() != null && signalLinkConfig.getBoolean(ConfigurationKeys.ENABLED)) {
                SignalStrengthLink debugLink = new SignalStrengthLink(
                        debugConfig.getString(ConfigurationKeys.TWILIO_ACCOUNT_SID),
                        debugConfig.getString(ConfigurationKeys.TWILIO_TOKEN)
                );

                Executors.newSingleThreadScheduledExecutor(
                        new ThreadFactoryBuilder()
                                .setNameFormat("debug-signalstrength-link-%d")
                                .setDaemon(true)
                                .build())
                        .scheduleAtFixedRate(() -> {
                            try {
                                String trackerName = signalLinkConfig.getString(ConfigurationKeys.TRACKER_NAME);
                                String body = "Tracker [" + trackerName + "] ";

                                Tracker tracker = nzyme.getTrackerManager().getTrackers()
                                        .get(trackerName);
                                if (tracker == null || tracker.getLastSeen().isBefore(DateTime.now().minusSeconds(TrackerManager.DARK_TIMEOUT_SECONDS))) {
                                    body += "offline.";
                                } else {
                                    body += "at RSSI <" + tracker.getRssi() + "> (" + (Math.round(tracker.getRssi() / 255.0 * 100)) + "%) "
                                            + "[" + tracker.getBanditCount() + "/" + nzyme.getContactManager().getBanditList().size() + " bandits].";
                                }

                                LOG.info("Sending signal link debug message: [{}]", body);

                                debugLink.report(
                                        signalLinkConfig.getString(ConfigurationKeys.PHONE_FROM),
                                        signalLinkConfig.getString(ConfigurationKeys.PHONE_TO),
                                        body
                                );
                            } catch(Exception e) {
                                LOG.error("Could not send Signal Link debug message.", e);
                            }
                        }, 30, 30, TimeUnit.SECONDS);
            }
        }
    }

}
