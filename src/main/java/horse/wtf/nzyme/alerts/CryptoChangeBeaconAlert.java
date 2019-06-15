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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CryptoChangeBeaconAlert extends Alert {

    private static final String DESCRIPTION = "The network is advertised with an unexpected wireless security mechanism. " +
            "This could indicate that an attacker is spoofing your SSID (network name) but does not know the correct password. Without the correct password, " +
            "clients will not connect. The attacker might be trying to simply leave out the password (note that most modern devices will refuse to connect " +
            "to a network that used to have a password but suddenly does not have one) or try a downgrade attack to exploit less secure mechanisms. It could " +
            "also indicate spoofing without attempting to properly replicate the original security mechanisms.";
    private static final String DOC_LINK = "guidance-CRYPTO_CHANGE_NEW";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("A legitimate configuration change of an access point could have caused this.");
    }};
    private CryptoChangeBeaconAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, Dot11Probe probe) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, probe);
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised with unexpected security settings [" + getEncounteredSecurity() + "].";
    }

    @Override
    public Type getType() {
        return Type.CRYPTO_CHANGE_BEACON;
    }

    public String getSSID() {
        return (String) getFields().get(Keys.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(Keys.BSSID);
    }

    public String getEncounteredSecurity() {
        return (String) getFields().get(Keys.ENCOUNTERED_SECURITY);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof CryptoChangeBeaconAlert)) {
            return false;
        }

        CryptoChangeBeaconAlert a = (CryptoChangeBeaconAlert) alert;

        return a.getSSID().equals(this.getSSID())
                && a.getBSSID().equals(this.getBSSID())
                && a.getEncounteredSecurity().equals(this.getEncounteredSecurity());
    }

    public static CryptoChangeBeaconAlert create(String ssid, String bssid, String encounteredSecurity, Dot11MetaInformation meta, Dot11Probe probe) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(Keys.SSID, ssid);
        fields.put(Keys.BSSID, bssid.toLowerCase());
        fields.put(Keys.ENCOUNTERED_SECURITY, encounteredSecurity);
        fields.put(Keys.CHANNEL, meta.getChannel());
        fields.put(Keys.FREQUENCY, meta.getFrequency());
        fields.put(Keys.ANTENNA_SIGNAL, meta.getAntennaSignal());

        return new CryptoChangeBeaconAlert(DateTime.now(), Subsystem.DOT_11, fields.build(), probe);
    }

}
