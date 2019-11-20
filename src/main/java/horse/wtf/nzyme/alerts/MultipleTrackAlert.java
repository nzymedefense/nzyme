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

package horse.wtf.nzyme.alerts;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.notifications.FieldNames;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultipleTrackAlert extends Alert {

    private static final String DESCRIPTION = "One of our stations is transmitting with more than one signal track. This could indicate that an attacker " +
            "is spoofing the station but with a different signal strength than the legitimate station. If this is an attacker, the difference in signal " +
            "strength is usually caused by different physical locations of attacker and legitimate station.";
    private static final String DOC_LINK = "guidance-MULTIPLE_TRACKS";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("A sudden change in the physical radio frequency environment can cause new tracks to appear. Monitor the signal track behavior long-term to spot normal changes in track behavior.");
        add("A station with adaptive transmit power can cause new tracks to be detected.");
        add("A physical relocation or configuration change of the station can cause the signal strength to change and new tracks to appear.");
    }};

    private MultipleTrackAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, null, false);
    }

    @Override
    public String getMessage() {
        return "Multiple tracks detected for our SSID [" + getSSID() + "] on [" + getBSSID() + "], channel [" + getChannel() + "]. Tracks: " + getTrackCount();
    }

    @Override
    public Type getType() {
        return Type.MULTIPLE_SIGNAL_TRACKS;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    public int getChannel() {
        return (int) getFields().get(FieldNames.CHANNEL);
    }

    public int getTrackCount() {
        return (int) getFields().get(FieldNames.TRACK_COUNT);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof MultipleTrackAlert)) {
            return false;
        }

        MultipleTrackAlert a = (MultipleTrackAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID()) && a.getChannel() == this.getChannel();
    }

    public static MultipleTrackAlert create(@NotNull String ssid, String bssid, int channel, int trackCount) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.TRACK_COUNT, trackCount);

        return new MultipleTrackAlert(DateTime.now(), Subsystem.DOT_11, fields.build());
    }

}
