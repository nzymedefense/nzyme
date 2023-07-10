package app.nzyme.core.dot11.tracks;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TrackDetector {

    public static final TrackDetectorConfig DEFAULT_CONFIG = TrackDetectorConfig.create(
            20, 9, 8);

    public List<Track> detect(List<List<Long>> zValues, List<DateTime> yValues, TrackDetectorConfig config) {
        /*
         * For each Y measurement (time), look at each X measurement and start a track if Y for the
         * coordinates are > FRAME_THRESHOLD. Keep the track active until coordinates Y is < FRAME_THRESHOLD
         * for more than GAP_THRESHOLD times.
         */

        int yIdx = 0;

        Map<Integer, List<PartialTrack>> partialTracks = Maps.newHashMap();
        for (List<Long> line : zValues) {
            DateTime y = yValues.get(yIdx);
            int x = -100;
            int trackLength = 0;
            int gapLength = 0;
            int trackStart = -1;

            for (Long z : line) {
                if (z > config.frameThreshold() && x != -100) {
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

                        if (gapLength >= config.gapThreshold() || x == 0) {
                            PartialTrack partialTrack = PartialTrack.create( y, trackStart, x-config.gapThreshold()+2);

                            if (!partialTracks.containsKey(partialTrack.averageSignal())) {
                                partialTracks.put(partialTrack.averageSignal(), Lists.newArrayList());
                            }
                            partialTracks.get(partialTrack.averageSignal()).add(partialTrack);

                            // Friendship with track ended.
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
         * Take all partial tracks, by center line (center line is the average signal strength) and try to
         * feed them into buckets using the SIGNAL_CENTER_LINE_JITTER: Aggregate all center lines that fit
         * within the SIGNAL_CENTERLINE_JITTER on the lower and higher side of the signal into a general
         * track.
         */
        Map<Integer, List<PartialTrack>> centerlineAveragedTracks = Maps.newHashMap();
        for (Map.Entry<Integer, List<PartialTrack>> partialTrack : partialTracks.entrySet()) {
            int partialCenterline = partialTrack.getKey();

            // Find a possibly existing centerline track that we can add this partial track to.
            Optional<Integer> matchingCenterline = Optional.empty();
            for (Integer existingCenterline : centerlineAveragedTracks.keySet()) {
                if (partialCenterline >= existingCenterline-config.signalCenterlineJitter() && partialCenterline <= existingCenterline+ config.signalCenterlineJitter()) {
                    matchingCenterline = Optional.of(existingCenterline);
                }
            }

            if (matchingCenterline.isPresent()) {
                centerlineAveragedTracks.get(matchingCenterline.get()).addAll(partialTrack.getValue());
            } else {
                centerlineAveragedTracks.put(partialCenterline, partialTrack.getValue());
            }

        }

        /*
         * Determine maximum and minimum values of aggregated averaged centerlined tracks, including first and
         * last appearance. These summaries can be used by the frontend to draw boxes on the Y (date) and X
         * (max/signal) axis of a waterfall histogram.
         */
        ImmutableList.Builder<Track> tracks = new ImmutableList.Builder<>();
        for (Map.Entry<Integer, List<PartialTrack>> aggregated : centerlineAveragedTracks.entrySet()) {
            PartialTrack first = aggregated.getValue().get(0);

            DateTime start = first.timestamp();
            DateTime end = first.timestamp();
            int minSignal = first.minSignal();
            int maxSignal = first.maxSignal();

            // Find track specifications.
            for (PartialTrack track : aggregated.getValue()) {
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
            tracks.add(Track.create(start, end, aggregated.getKey(), minSignal, maxSignal));
        }

        return tracks.build();
    }

    @AutoValue
    public static abstract class TrackDetectorConfig {

        public abstract int frameThreshold();
        public abstract int gapThreshold();
        public abstract int signalCenterlineJitter();

        public static TrackDetectorConfig create(int frameThreshold, int gapThreshold, int signalCenterlineJitter) {
            return builder()
                    .frameThreshold(frameThreshold)
                    .gapThreshold(gapThreshold)
                    .signalCenterlineJitter(signalCenterlineJitter)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_TrackDetector_TrackDetectorConfig.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder frameThreshold(int frameThreshold);

            public abstract Builder gapThreshold(int gapThreshold);

            public abstract Builder signalCenterlineJitter(int signalCenterlineJitter);

            public abstract TrackDetectorConfig build();
        }
    }


}
