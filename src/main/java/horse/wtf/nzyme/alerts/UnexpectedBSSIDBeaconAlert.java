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

public class UnexpectedBSSIDBeaconAlert extends Alert {

    private static final String DESCRIPTION = "A station with an unexpected BSSID (hardware address) is advertising one of our SSIDs (network name). This could " +
            "be a rogue access point trying to lure users to connect to it by making it look like a legitimate access point of a wireless network that " +
            "users trust. Note that sophisticated attackers will likely not cause this kind of alert because they would act like a legitimate access point " +
            "by sending frames with a spoofed BSSID.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_BEACON_BSSID";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A new access point was installed and the nzyme configuration has not been updated yet.");
    }};

    private UnexpectedBSSIDBeaconAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised with beacon frame by unexpected BSSID [" + getBSSID() + "].";
    }

    @Override
    public TYPE getType() {
        return TYPE.UNEXPECTED_BSSID_BEACON;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnexpectedBSSIDBeaconAlert)) {
            return false;
        }

        UnexpectedBSSIDBeaconAlert a = (UnexpectedBSSIDBeaconAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID());
    }

    public static UnexpectedBSSIDBeaconAlert create(DateTime timestamp, @NotNull String ssid, String bssid, int channel, int frequency, int antennaSignal, long frameCount) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);

        return new UnexpectedBSSIDBeaconAlert(timestamp, Subsystem.DOT_11, fields.build(), frameCount);
    }

}
