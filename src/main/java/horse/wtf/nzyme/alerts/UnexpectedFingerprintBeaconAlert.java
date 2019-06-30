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
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnexpectedFingerprintBeaconAlert extends Alert {

    private static final String DESCRIPTION = "The network is advertised with a fingerprint that is not in the list of configured expected fingerprints. This could " +
            "indicate that a possible attacker is spoofing your network.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_FINGERPRINT";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A legitimate change of the access point configuration took place and the nzyme configuration has not been updated.");
    }};

    private UnexpectedFingerprintBeaconAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, Dot11Probe probe) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, probe);
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised by a device with unexpected fingerprint [" + getFingerprint() + "]";
    }

    @Override
    public Alert.Type getType() {
        return Type.UNEXPECTED_FINGERPRINT_BEACON;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    public String getFingerprint() {
        return (String) getFields().get(FieldNames.BANDIT_FINGERPRINT);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnexpectedFingerprintBeaconAlert)) {
            return false;
        }

        UnexpectedFingerprintBeaconAlert a = (UnexpectedFingerprintBeaconAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getFingerprint().equals(this.getFingerprint());
    }

    public static UnexpectedFingerprintBeaconAlert create(@NotNull String ssid, String fingerprint, String bssid, Dot11MetaInformation meta, Dot11Probe probe) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        if (Strings.isNullOrEmpty(fingerprint)) {
            throw new IllegalArgumentException("This alert cannot be raised for empty fingerprints.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BANDIT_FINGERPRINT, fingerprint);
        fields.put(FieldNames.CHANNEL, meta.getChannel());
        fields.put(FieldNames.FREQUENCY, meta.getFrequency());
        fields.put(FieldNames.ANTENNA_SIGNAL, meta.getAntennaSignal());

        return new UnexpectedFingerprintBeaconAlert(DateTime.now(), Subsystem.DOT_11, fields.build(), probe);
    }

}
