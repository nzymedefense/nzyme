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

import com.google.common.collect.ImmutableMap;
import app.nzyme.core.Subsystem;
import app.nzyme.core.notifications.FieldNames;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeauthFloodAlert extends Alert {

    private static final String DESCRIPTION = "More deauthentication or disassociation frames than usual were recorded. " +
            "The expected number of frames can be different in any environment and is configured in your nzyme.conf file at " +
            "\"deauth_monitor.global_threshold\". Deauthentication attacks are an attempt to force a device to disconnect from " +
            "a legitimate access point and re-connect to a rogue access point controlled by an attacker. Such attacks can also be " +
            "used for jamming, rendering the WiFi environment unusable through mass disconnections. Note that deauthentication " +
            "and disassociation frames are an important part of WiFi communication and their occurrence is normal. This is why you " +
            "have to find and configure a threshold that defines at what level an attack might be taking place.";
    private static final String DOC_LINK = "guidance-DEAUTH_FLOOD";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("Your threshold may be configured too low and normal deauthentication/disassociation activity in your network is causing the alert.");
    }};

    private DeauthFloodAlert(DateTime timestamp, Map<String, Object> fields) {
        super(timestamp, Subsystem.DOT_11, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, false, -1);
    }

    @Override
    public String getMessage() {
        return "Deauthentication flood detected. Rate <" + getDeauthRate() + "> is over threshold <" + getGlobalThreshold() + ">.";
    }

    @Override
    public TYPE getType() {
        return TYPE.DEAUTH_FLOOD;
    }

    @Override
    public boolean sameAs(Alert alert) {
        return alert instanceof DeauthFloodAlert;
    }

    public int getDeauthRate() {
        return (int) getFields().get(FieldNames.DEAUTH_RATE);
    }

    public int getGlobalThreshold() {
        return (int) getFields().get(FieldNames.GLOBAL_THRESHOLD);
    }

    public static DeauthFloodAlert create(DateTime firstSeen, int rate, int threshold) {
        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.DEAUTH_RATE, rate);
        fields.put(FieldNames.GLOBAL_THRESHOLD, threshold);

        return new DeauthFloodAlert(firstSeen, fields.build());
    }

}
