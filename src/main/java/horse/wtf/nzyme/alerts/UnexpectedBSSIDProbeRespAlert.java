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

public class UnexpectedBSSIDProbeRespAlert extends Alert {

    private static final String DESCRIPTION = "A station with an unexpected BSSID (hardware address) is replying to devices that are looking for one of our " +
            "networks (probing). This could be a rogue access point trying to lure users to connect to it by making it look like a legitimate access point of " +
            "a wireless network that users trust. Note that sophisticated attackers will likely not cause this kind of alert because they would act like a " +
            "legitimate access point by sending frames with a spoofed BSSID.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_PROBERESP_BSSID";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A new access point was installed and the nzyme configuration has not been updated yet.");
    }};

    private UnexpectedBSSIDProbeRespAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised with probe response frame by unexpected BSSID [" + getBSSID() + "] for [" + getDestination() + "]";
    }

    @Override
    public TYPE getType() {
        return TYPE.UNEXPECTED_BSSID_PROBERESP;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    public String getDestination() {
        return (String) getFields().get(FieldNames.DESTINATION);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnexpectedBSSIDProbeRespAlert)) {
            return false;
        }

        UnexpectedBSSIDProbeRespAlert a = (UnexpectedBSSIDProbeRespAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID());
    }

    public static UnexpectedBSSIDProbeRespAlert create(DateTime firstSeen, @NotNull String ssid, String bssid, String destination, int channel, int frequency, int antennaSignal, long frameCount) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.DESTINATION, destination.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);

        return new UnexpectedBSSIDProbeRespAlert(firstSeen, Subsystem.DOT_11, fields.build(), frameCount);
    }

}
