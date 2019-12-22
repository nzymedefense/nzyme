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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KnownBanditFingerprintProbeRespAlert extends Alert {

    private static final String DESCRIPTION = "A station with a known bandit fingerprint is replying to devices that are looking for a network. " +
            "A bandit device is usually a device like a rogue access point or other gear used by threat actors. Note that this alert fires for any " +
            "detected bandit device and not only for those impersonating our networks because networks with different names could be used to lure " +
            "targets/people.";
    private static final String DOC_LINK = "guidance-KNOWN-BANDIT-FINGERPRINT";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("A legitimate device could have the same fingerprint as a known bandit device. This is very unlikely in practice.");
    }};

    private final List<String> banditNames;
    private final String fingerprint;

    private KnownBanditFingerprintProbeRespAlert(List<String> banditNames, String fingerprint, DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);

        this.banditNames = banditNames;
        this.fingerprint = fingerprint;
    }

    @Override
    public String getMessage() {
        String ssid = getSSID() == null ? "hidden/broadcast" : getSSID();

        return "SSID [" + ssid + "] was advertised by a known bandit device of type: [" + Joiner.on(",").join(banditNames) + "] with fingerprint [" + fingerprint + "]";
    }

    @Override
    public TYPE getType() {
        return TYPE.KNOWN_BANDIT_FINGERPRINT_PROBERESP;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof KnownBanditFingerprintProbeRespAlert)) {
            return false;
        }

        KnownBanditFingerprintProbeRespAlert a = (KnownBanditFingerprintProbeRespAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getFingerprint().equals(this.getFingerprint());
    }

    public static KnownBanditFingerprintProbeRespAlert create(DateTime firstSeen, List<String> banditNames, String fingerprint, @Nullable String ssid, String bssid, int channel, int frequency, int antennaSignal, long frameCount) {
        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();

        if (!Strings.isNullOrEmpty(ssid)) {
            fields.put(FieldNames.SSID, ssid);
        }

        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);
        fields.put(FieldNames.BANDIT_NAMES, Joiner.on(",").join(banditNames));
        fields.put(FieldNames.BANDIT_FINGERPRINT, fingerprint);

        return new KnownBanditFingerprintProbeRespAlert(banditNames, fingerprint, firstSeen, Subsystem.DOT_11, fields.build(), frameCount);
    }


}
