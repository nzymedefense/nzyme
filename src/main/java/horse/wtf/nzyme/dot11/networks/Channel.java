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

package horse.wtf.nzyme.dot11.networks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.networks.signalstrength.SignalStrengthTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@AutoValue
public abstract class Channel {

    @JsonProperty("channel_number")
    public abstract int channelNumber();

    @JsonProperty("bssid")
    public abstract String bssid();

    @JsonProperty("ssid")
    public abstract String ssid();

    @JsonProperty("total_frames")
    public abstract AtomicLong totalFrames();


    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    @JsonIgnore
    public abstract SignalStrengthTable signalStrengthTable();

    @JsonIgnore
    public void registerFingerprint(String fingerprint) {
        if (!fingerprints().contains(fingerprint)) {
            fingerprints().add(fingerprint);
        }
    }

    @JsonIgnore
    public abstract AtomicLong totalFramesRecent();

    @JsonIgnore
    private AtomicLong previousTotalFramesRecent;

    @JsonProperty("total_frames_recent")
    public AtomicLong getTotalFramesRecent() {
        // If we are in the first cycle, return current active counter.
        if (this.previousTotalFramesRecent == null) {
            return totalFramesRecent();
        } else {
            return previousTotalFramesRecent;
        }
    }

    @JsonIgnore
    public void cycleRecentFrames() {
        if (this.previousTotalFramesRecent == null) {
            this.previousTotalFramesRecent = new AtomicLong(0);
        }

        this.previousTotalFramesRecent.set(this.totalFramesRecent().get());
        this.totalFramesRecent().set(0);
    }

    public static Channel create(NzymeLeader nzyme,
                                 int channelNumber,
                                 String bssid,
                                 String ssid,
                                 AtomicLong totalFrames,
                                 AtomicLong totalFramesRecent,
                                 String fingerprint) {

        List<String> fingerprints = new ArrayList<>() {{
            if(fingerprint != null) {
                add(fingerprint);
            }
        }};

        return builder()
                .signalStrengthTable(new SignalStrengthTable(bssid, ssid, channelNumber, nzyme.getMetrics()))
                .bssid(bssid)
                .ssid(ssid)
                .channelNumber(channelNumber)
                .totalFrames(totalFrames)
                .totalFramesRecent(totalFramesRecent)
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Channel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder signalStrengthTable(SignalStrengthTable signalStrengthTable);

        public abstract Builder bssid(String bssid);

        public abstract Builder ssid(String ssid);

        public abstract Builder channelNumber(int channelNumber);

        public abstract Builder totalFrames(AtomicLong totalFrames);

        public abstract Builder totalFramesRecent(AtomicLong totalFramesRecent);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Channel build();
    }


}
