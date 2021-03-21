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

public class UnexpectedChannelProbeRespAlert extends Alert {


    private static final String DESCRIPTION = "One of our stations is replying to devices that are looking for known access points (probing) on a channel that is not in " +
            "the list of configured expected channels. This could indicate that a possible attacker is not careful enough and does not limit spoofing to channels that are " +
            "in use by the legitimate access points.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_CHANNEL";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A legitimate change of the access point configuration took place and the nzyme configuration has not been updated.");
        add("Some access points will dynamically choose channels based on RF spectrum congestion. Always include all possibly used channels in the nzyme configuration.");
    }};

    private UnexpectedChannelProbeRespAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised with a probe response frame on an unexpected channel.";
    }

    @Override
    public TYPE getType() {
        return TYPE.UNEXPECTED_CHANNEL_PROBERESP;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public int getChannel() {
        return (int) getFields().get(FieldNames.CHANNEL);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnexpectedChannelProbeRespAlert)) {
            return false;
        }

        UnexpectedChannelProbeRespAlert a = (UnexpectedChannelProbeRespAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getChannel() == this.getChannel();
    }

    public static UnexpectedChannelProbeRespAlert create(DateTime firstSeen, @NotNull String ssid, String bssid, int channel, int frequency, int antennaSignal, long frameCount) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);

        return new UnexpectedChannelProbeRespAlert(firstSeen, Subsystem.DOT_11, fields.build(), frameCount);
    }

}
