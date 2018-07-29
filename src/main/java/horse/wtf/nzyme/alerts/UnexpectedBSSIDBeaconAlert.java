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
import org.joda.time.DateTime;

import java.util.Map;

public class UnexpectedBSSIDBeaconAlert extends Alert {

    private UnexpectedBSSIDBeaconAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields) {
        super(timestamp, subsystem, fields);
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

    public static UnexpectedBSSIDBeaconAlert create(String ssid, String bssid, Dot11MetaInformation meta) {
        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(Keys.SSID, ssid);
        fields.put(Keys.BSSID, bssid.toLowerCase());
        fields.put(Keys.CHANNEL, meta.getChannel());
        fields.put(Keys.FREQUENCY, meta.getFrequency());
        fields.put(Keys.ANTENNA_SIGNAL, meta.getAntennaSignal());

        return new UnexpectedBSSIDBeaconAlert(DateTime.now(), Subsystem.DOT_11, fields.build());
    }

}
