/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
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

public class UnexpectedFingerprintProbeRespAlert extends Alert {

    private static final String DESCRIPTION = "One of our stations is replying to devices that are looking for known access points (probing) with a fingerprint " +
            "that is not in the list of configured expected fingerprints. This could indicate that a possible attacker is spoofing your network.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_FINGERPRINT";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A legitimate change of the access point configuration took place and the nzyme configuration has not been updated.");
    }};

    private UnexpectedFingerprintProbeRespAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised with a probe response by a device with unexpected fingerprint [" + getFingerprint() + "]";
    }

    @Override
    public TYPE getType() {
        return TYPE.UNEXPECTED_FINGERPRINT_PROBERESP;
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
        if (!(alert instanceof UnexpectedFingerprintProbeRespAlert)) {
            return false;
        }

        UnexpectedFingerprintProbeRespAlert a = (UnexpectedFingerprintProbeRespAlert) alert;

        return a.getSSID().equals(this.getSSID())
                && a.getBSSID().equals(this.getBSSID())
                && a.getFingerprint().equals(this.getFingerprint());
    }

    public static UnexpectedFingerprintProbeRespAlert create(DateTime firstSeen, @NotNull String ssid, String fingerprint, String bssid, int channel, int frequency, int antennaSignal, long frameCount) {
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
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);

        return new UnexpectedFingerprintProbeRespAlert(firstSeen, Subsystem.DOT_11, fields.build(), frameCount);
    }

}