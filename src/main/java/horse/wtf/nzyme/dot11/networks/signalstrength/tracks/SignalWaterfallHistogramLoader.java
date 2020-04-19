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

package horse.wtf.nzyme.dot11.networks.signalstrength.tracks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.dot11.networks.signalstrength.SignalIndexHistogramHistoryDBEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SignalWaterfallHistogramLoader {

    private static final Logger LOG = LogManager.getLogger(SignalWaterfallHistogramLoader.class);

    public static final String HISTOGRAM_HISTORY_QUERY = "SELECT histogram, created_at FROM sigidx_histogram_history " +
            "WHERE bssid = ? AND ssid = ? AND channel = ? AND created_at > (current_timestamp at time zone 'UTC' - interval <lookback>) " +
            "ORDER BY created_at ASC";

    private final NzymeLeader nzyme;

    public SignalWaterfallHistogramLoader(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    public SignalWaterfallHistogram load(BSSID b, SSID s, Channel c, int seconds) {
        List<SignalIndexHistogramHistoryDBEntry> values = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(HISTOGRAM_HISTORY_QUERY)
                        .bind(0, b.bssid())
                        .bind(1, s.name())
                        .bind(2, c.channelNumber())
                        .define("lookback", "'" + seconds + " seconds'") // TODO this is fucked
                        .mapTo(SignalIndexHistogramHistoryDBEntry.class)
                        .list()
        );

        // Transform the histogram string blobs from the database to structured data.
        List<List<Long>> z = Lists.newArrayList();
        List<DateTime> y = Lists.newArrayList();
        for (SignalIndexHistogramHistoryDBEntry value : values) {
            try {
                List<Long> entries = new ArrayList<>();
                Map<Integer, Long> tempReduced = Maps.newHashMap();
                Map<Integer, Long> histogram = nzyme.getObjectMapper().readValue(value.histogram(), new TypeReference<Map<Integer, Long>>(){});

                for (Map.Entry<Integer, Long> x : histogram.entrySet()) {
                    tempReduced.put(x.getKey(), x.getValue());
                }

                for(int cnt = -100; cnt < 0; cnt++) {
                    entries.add(tempReduced.getOrDefault(cnt, 0L));
                }

                z.add(entries);
                y.add(value.createdAt().withSecondOfMinute(0));
            } catch (Exception e) {
                LOG.error("Could not parse histogram blob to structured data for BSSID [{}].", b, e);
            }
        }

        // X Axis.
        List<Integer> x = Lists.newArrayList();
        for(int cnt = -100; cnt < 0; cnt++) {
            x.add(cnt);
        }

        return SignalWaterfallHistogram.create(z, x, y);
    }

}
