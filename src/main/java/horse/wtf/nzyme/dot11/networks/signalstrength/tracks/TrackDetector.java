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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/*
 * Give me a ping, Vasily. One ping only, please.
 */
public class TrackDetector {

    private static final Logger LOG = LogManager.getLogger(TrackDetector.class);

    public static final int FRAME_THRESHOLD = 20;
    public static final int GAP_THRESHOLD = 8;

    private final SignalWaterfallHistogram histogram;

    public TrackDetector(SignalWaterfallHistogram histogram) throws IllegalArgumentException {
        this.histogram = histogram;
    }

    public List<Track> detect() {
        /*
         * For each Y measurement (time), look at each X measurement and start a track if Y for the
         * coordinates are > FRAME_THRESHOLD. Keep the track active until coordinates Y is < FRAME_THRESHOLD
         * for more than GAP_THRESHOLD times.
         */

        int yIdx = 0;

        Map<Integer, List<PartialTrack>> partialTracks = Maps.newHashMap();
        for (List<Long> line : histogram.z()) {
            DateTime y = histogram.y().get(yIdx);
            int x = -100;
            int trackLength = 0;
            int gapLength = 0;
            int trackStart = -1;
            int trackIdx = 0;

            for (Long z : line) {
                if (z > FRAME_THRESHOLD && x != -100) {
                    // Signal.
                    if (trackLength == 0) {
                        // New track identified.
                        trackStart = x;
                    }

                    // Existing track continued.
                    trackLength++;
                } else {
                    // We are in a signal gap or at end of signal strength spectrum.
                    if (trackLength > 0) {
                        // We are on a track.
                        gapLength++;

                        if (gapLength >= GAP_THRESHOLD || x == 0) {
                            PartialTrack partialTrack = PartialTrack.create("sst-" + trackIdx, y, trackStart, x-GAP_THRESHOLD+2);

                            LOG.debug(partialTrack);
                            if (!partialTracks.containsKey(partialTrack.averageSignal())) {
                                partialTracks.put(partialTrack.averageSignal(), Lists.newArrayList());
                            }
                            partialTracks.get(partialTrack.averageSignal()).add(partialTrack);

                            // Friendship with track ended.
                            trackIdx++;
                            trackLength = 0;
                            gapLength = 0;
                        }
                    }
                }

                // Line has been processed.
                x++;
            }

            yIdx++;
        }

        /*
         * TODO
         *  
         * Take all partial tracks, by center line (center line is the average signal strength) and try to
         * feed them into buckets using the SIGNAL_CENTER_LINE_JITTER: Aggregate all center lines that fit
         * within the SIGNAL_CENTER_LINE_JITTER on the lower and higher side of the signal into a general
         * track.
         *
         * Using this track, including timestamps for the Y axis,the frontend can draw boxes.
         */
        ImmutableList.Builder<Track> tracks = new ImmutableList.Builder<>();
        for (Map.Entry<String, List<PartialTrack>> partialTrack : partialTracks.entrySet()) {
            PartialTrack first = partialTrack.getValue().get(0);

            DateTime start = first.timestamp();
            DateTime end = first.timestamp();
            int minSignal = first.minSignal();
            int maxSignal = first.maxSignal();

            // Find track specifications.
            for (PartialTrack track : partialTrack.getValue()) {
                if (track.timestamp().isBefore(start)) {
                    start = track.timestamp();
                }

                if (track.timestamp().isAfter(end)) {
                    end = track.timestamp();
                }

                if (track.minSignal() < minSignal) {
                    minSignal = track.minSignal();
                }

                if (track.maxSignal() > maxSignal) {
                    maxSignal = track.maxSignal();
                }
            }

            // Add the final track.
            tracks.add(Track.create(partialTrack.getKey(), start, end, minSignal, maxSignal));
        }


        return tracks.build();
    }

}
