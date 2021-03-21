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
import horse.wtf.nzyme.notifications.FieldNames;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeaconRateAnomalyAlert extends Alert {

    private static final String DESCRIPTION = "One of our networks is sending beacon frames in a higher frequency than expected. " +
            "This could indicate that an attacker is spoofing one of our access points and that their additional beacon frames are leading " +
            "to this increase.";
    private static final String DOC_LINK = "guidance-BEACON_RATE";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("The beacon frequency threshold is a manual nzyme config and might be set to a wrong value.");
        add("A legitimate access point might have decided to increase the beacon rate. Try to adapt the beacon frequency threshold accordingly.");
    }};

    private BeaconRateAnomalyAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, false, -1);
    }

    @Override
    public String getMessage() {
        return "Beacon rate anomaly detected for our SSID [" + getSSID() + "] on [" + getBSSID() + "]. Rate <" + getBeaconRate() + "> is over threshold <" + getBeaconRateThreshold() + ">.";
    }

    @Override
    public TYPE getType() {
        return TYPE.BEACON_RATE_ANOMALY;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    public double getBeaconRate() {
        return (double) getFields().get(FieldNames.BEACON_RATE);
    }

    public int getBeaconRateThreshold() {
        return (int) getFields().get(FieldNames.BEACON_RATE_THRESHOLD);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof BeaconRateAnomalyAlert)) {
            return false;
        }

        BeaconRateAnomalyAlert a = (BeaconRateAnomalyAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID());
    }

    public static BeaconRateAnomalyAlert create(DateTime firstSeen, @NotNull String ssid, String bssid, double beaconRate, int beaconRateThreshold) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.BEACON_RATE, beaconRate);
        fields.put(FieldNames.BEACON_RATE_THRESHOLD, beaconRateThreshold);

        return new BeaconRateAnomalyAlert(firstSeen, Subsystem.DOT_11, fields.build());
    }

}
