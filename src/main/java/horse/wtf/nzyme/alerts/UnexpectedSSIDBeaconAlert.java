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
import horse.wtf.nzyme.configuration.Keys;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnexpectedSSIDBeaconAlert extends Alert  {

    private static final String DESCRIPTION = "One of our stations is advertising a SSID (network name) that we did not expect. An attacker might have gained access " +
            "to access point configuration and could have created a new wireless network. This alert can also indicate actions of an attacker who is not careful with " +
            "the spoofing of BSSIDs (hardware addresses) of access points.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_BEACON_SSID";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A new network (for example, a guest network) was created by a legitimate administrator and the nzyme configuration has not been updated yet.");
    }};

    private UnexpectedSSIDBeaconAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, Dot11Probe probe) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, probe);
    }

    @Override
    public String getMessage() {
        return "Our BSSID [" + getBSSID() + "] advertised unexpected SSID [" + getSSID() + "] with beacon frame.";
    }

    @Override
    public Alert.Type getType() {
        return Type.UNEXPECTED_SSID_BEACON;
    }

    public String getSSID() {
        return (String) getFields().get(Keys.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(Keys.BSSID);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnexpectedSSIDBeaconAlert)) {
            return false;
        }

        UnexpectedSSIDBeaconAlert a = (UnexpectedSSIDBeaconAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID());
    }

    public static UnexpectedSSIDBeaconAlert create(@NotNull String ssid, String bssid, Dot11MetaInformation meta, Dot11Probe probe) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(Keys.SSID, ssid);
        fields.put(Keys.BSSID, bssid.toLowerCase());
        fields.put(Keys.CHANNEL, meta.getChannel());
        fields.put(Keys.FREQUENCY, meta.getFrequency());
        fields.put(Keys.ANTENNA_SIGNAL, meta.getAntennaSignal());

        return new UnexpectedSSIDBeaconAlert(DateTime.now(), Subsystem.DOT_11, fields.build(), probe);
    }

}
