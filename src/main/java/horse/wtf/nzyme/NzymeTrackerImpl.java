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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.configuration.tracker.TrackerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;

public class NzymeTrackerImpl implements NzymeTracker {

    private static final Logger LOG = LogManager.getLogger(NzymeTrackerImpl.class);

    private final Version version;

    private final TrackerConfiguration configuration;

    private final GroundStation groundStation;

    public NzymeTrackerImpl(TrackerConfiguration configuration) {
        this.version = new Version();
        this.configuration = configuration;

        try {
            this.groundStation = new GroundStation(Role.TRACKER, configuration.nzymeId(), version.getVersion().toString(), configuration.trackerDevice());
        } catch(Exception e) {
            throw new RuntimeException("Tracker Device configuration failed.", e);
        }
    }

    @Override
    public void initialize() {
        LOG.info("Initializing nzyme tracker version: {}.", version.getVersionString());

        groundStation.onPingReceived(ping -> LOG.info("Received Ping!"));

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

}
