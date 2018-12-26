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

import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.configuration.Keys;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnexpectedBSSIDBeaconAlert extends Alert {

    private static final String DESCRIPTION = "A station with an unexpected BSSID (hardware address) is advertising one of our SSIDs (network name). This could " +
            "be a rogue access point trying to lure users to connect to it by making it look like a legitimate access point of a wireless network that " +
            "users trust. Note that sophisticated attackers will likely not cause this kind of alert because they would act like a legitimate access point " +
            "by sending frames with a spoofed BSSID.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_BEACON_BSSID";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A new access point was installed and the nzyme configuration has not been updated yet.");
    }};

    private UnexpectedBSSIDBeaconAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, Dot11Probe probe) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, probe);
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised with beacon frame by unexpected BSSID [" + getBSSID() + "].";
    }

    @Override
    public Type getType() {
        return Type.UNEXPECTED_BEACON_BSSID;
    }

    public String getSSID() {
        return (String) getFields().get(Keys.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(Keys.BSSID);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnexpectedBSSIDBeaconAlert)) {
            return false;
        }

        UnexpectedBSSIDBeaconAlert a = (UnexpectedBSSIDBeaconAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID());
    }

    public static UnexpectedBSSIDBeaconAlert create(String ssid, String bssid, Dot11MetaInformation meta, Dot11Probe probe) {
        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(Keys.SSID, ssid);
        fields.put(Keys.BSSID, bssid.toLowerCase());
        fields.put(Keys.CHANNEL, meta.getChannel());
        fields.put(Keys.FREQUENCY, meta.getFrequency());
        fields.put(Keys.ANTENNA_SIGNAL, meta.getAntennaSignal());

        return new UnexpectedBSSIDBeaconAlert(DateTime.now(), Subsystem.DOT_11, fields.build(), probe);
    }

}
