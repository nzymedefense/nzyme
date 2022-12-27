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

package app.nzyme.core.alerts;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import app.nzyme.core.Subsystem;
import app.nzyme.core.notifications.FieldNames;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnknownSSIDAlert extends Alert {

    private static final String DESCRIPTION = "An SSID (network name) that has not been seen before was detected. Nzyme " +
            "keeps a list of networks it has seen and this alert was triggered because a previously unknown network was " +
            "advertised. Note that this is very often a legitimate network (see false positives below) and should be treated " +
            "as a notice that needs further human investigation to determine if it is a potential threat or not.";
    private static final String DOC_LINK = "guidance-UNKNOWN_SSID";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A new and legitimate network might have been enabled by someone in the vicinity.");
        add("A legitimate network could have been in range temporarily. A common example is a car with smart functionality that brings its own WiFi network passing through the coverage area of nzyme.");
    }};

    private UnknownSSIDAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, false, 0);
    }

    @Override
    public String getMessage() {
        return "New SSID [" + getSSID() + "] detected.";
    }


    @Override
    public TYPE getType() {
        return TYPE.UNKNOWN_SSID;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnknownSSIDAlert)) {
            return false;
        }

        UnknownSSIDAlert a = (UnknownSSIDAlert) alert;

        return a.getSSID().equals(this.getSSID());
    }

    public static UnknownSSIDAlert create(DateTime firstSeen, @NotNull String ssid, String bssid, int channel, int frequency, int antennaSignal) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);

        return new UnknownSSIDAlert(firstSeen, Subsystem.DOT_11, fields.build());
    }

}
