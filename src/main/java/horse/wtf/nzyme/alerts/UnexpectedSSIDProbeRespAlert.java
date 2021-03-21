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

public class UnexpectedSSIDProbeRespAlert extends Alert {

    private static final String DESCRIPTION = "One of our stations is replying to devices that are looking for known access points (probing) with an SSID (network name) that we did " +
            "not expect. An attacker might have gained access to access point configuration and could have created a new wireless network. This alert can also indicate actions " +
            "of an attacker who is not careful with the spoofing of BSSIDs (hardware addresses) of access points.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_PROBERESP_SSID";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A new network (for example, a guest network) was created by a legitimate administrator and the nzyme configuration has not been updated yet.");
    }};

    private UnexpectedSSIDProbeRespAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);
    }

    @Override
    public String getMessage() {
        return "Our BSSID [" + getBSSID() + "] advertised unexpected SSID [" + getSSID() + "] with probe response frame.";
    }

    @Override
    public TYPE getType() {
        return TYPE.UNEXPECTED_SSID_PROBERESP;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnexpectedSSIDProbeRespAlert)) {
            return false;
        }

        UnexpectedSSIDProbeRespAlert a = (UnexpectedSSIDProbeRespAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID());
    }

    public static UnexpectedSSIDProbeRespAlert create(DateTime firstSeen, @NotNull String ssid, String bssid, int channel, int frequency, int antennaSignal, long frameCount) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);

        return new UnexpectedSSIDProbeRespAlert(firstSeen, Subsystem.DOT_11, fields.build(), frameCount);
    }


}
