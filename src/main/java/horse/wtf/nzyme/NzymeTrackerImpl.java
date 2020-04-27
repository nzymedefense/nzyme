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

package horse.wtf.nzyme;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.bandits.trackers.TrackerBanditManager;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.bandits.trackers.hid.LogHID;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.BanditBroadcastMessageHandler;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.configuration.tracker.TrackerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;

public class NzymeTrackerImpl implements NzymeTracker {

    private static final Logger LOG = LogManager.getLogger(NzymeTrackerImpl.class);

    private final Version version;

    private final TrackerConfiguration configuration;

    private final GroundStation groundStation;
    private final TrackerBanditManager banditManager;

    private final ObjectMapper om;

    public NzymeTrackerImpl(TrackerConfiguration configuration) {
        this.version = new Version();
        this.configuration = configuration;

        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.banditManager = new TrackerBanditManager(this);

        try {
            this.groundStation = new GroundStation(Role.TRACKER, configuration.nzymeId(), version.getVersion().toString(), banditManager, configuration.trackerDevice());
            this.groundStation.registerHID(new LogHID());
        } catch(Exception e) {
            throw new RuntimeException("Tracker Device configuration failed.", e);
        }
    }

    @Override
    public void initialize() {
        LOG.info("Initializing nzyme tracker version: {}.", version.getVersionString());

        this.groundStation.onBanditBroadcastReceived(new BanditBroadcastMessageHandler() {
            @Override
            public void handle(TrackerMessage.BanditBroadcast broadcast) {
                banditManager.registerBandit(broadcast);
            }
        });

        Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("ground-station-%d")
                        .build())
                .submit(groundStation);
    }

    @Override
    public void shutdown() {
        this.groundStation.stop();
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.om;
    }

}
