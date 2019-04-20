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

public class KnownBanditFingerprintBeaconAlert extends Alert {

    private static final String DESCRIPTION = "A station with a known bandit fingerprint is advertising a network. " +
            "A bandit device is usually a device like a rogue access point or other gear used by threat actors. Note that this alert fires for any " +
            "detected bandit device and not only for those impersonating our networks because networks with different names could be used to lure " +
            "targets/people.";
    private static final String DOC_LINK = "guidance-KNOWN-BANDIT-FINGERPRINT";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("A legitimate device could have the same fingerprint as a known bandit device. This is very unlikely in practice.");
    }};

    private final String banditName;
    private final String fingerprint;

    private KnownBanditFingerprintBeaconAlert(String banditName, String fingerprint, DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, Dot11Probe probe) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, probe);

        this.banditName = banditName;
        this.fingerprint = fingerprint;
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised by a known bandit device of type: [" + banditName + "] with fingerprint [" + fingerprint + "]";
    }

    @Override
    public Alert.Type getType() {
        return Type.KNOWN_BANDIT_FINGERPRINT_BEACON;
    }

    public String getSSID() {
        return (String) getFields().get(Keys.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(Keys.BSSID);
    }

    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof KnownBanditFingerprintBeaconAlert)) {
            return false;
        }

        KnownBanditFingerprintBeaconAlert a = (KnownBanditFingerprintBeaconAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getFingerprint().equals(this.getFingerprint());
    }

    public static KnownBanditFingerprintBeaconAlert create(String banditName, String fingerprint, String ssid, String bssid, Dot11MetaInformation meta, Dot11Probe probe) {
        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(Keys.SSID, ssid);
        fields.put(Keys.BSSID, bssid.toLowerCase());
        fields.put(Keys.CHANNEL, meta.getChannel());
        fields.put(Keys.FREQUENCY, meta.getFrequency());
        fields.put(Keys.ANTENNA_SIGNAL, meta.getAntennaSignal());
        fields.put(Keys.BANDIT_NAME, banditName);
        fields.put(Keys.BANDIT_FINGERPRINT, fingerprint);

        return new KnownBanditFingerprintBeaconAlert(banditName, fingerprint, DateTime.now(), Subsystem.DOT_11, fields.build(), probe);
    }

}