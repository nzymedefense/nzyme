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
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.deception.traps.Trap;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProbeRequestTrapResponseAlert extends Alert {

    private static final String DESCRIPTION = "A device responded to our probe request trap (" + Trap.Type.PROBE_REQUEST_1 + "). This " +
            "clearly indicates that an attacker is trying to lure another device to connect to their rogue access point.";

    private static final String DOC_LINK = "guidance-TRAP_PROBE_REQUEST_1";

    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("This can only be a false positive if you used a legitimate SSID in the trap configuration.");
    }};

    private ProbeRequestTrapResponseAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);
    }

    @Override
    public String getMessage() {
        return "Device [" + getBSSID() + "] responded to our probe-request trap (" + Trap.Type.PROBE_REQUEST_1 + ") for [" + getSSID() + "].";
    }

    @Override
    public TYPE getType() {
        return TYPE.PROBE_RESPONSE_TRAP_1;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof ProbeRequestTrapResponseAlert)) {
            return false;
        }

        ProbeRequestTrapResponseAlert a = (ProbeRequestTrapResponseAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID());
    }

    public static ProbeRequestTrapResponseAlert create(DateTime firstSeen, @NotNull String ssid, String bssid, int channel, int frequency, int antennaSignal, long frameCount) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);

        return new ProbeRequestTrapResponseAlert(firstSeen, Subsystem.DOT_11, fields.build(), frameCount);
    }

}
