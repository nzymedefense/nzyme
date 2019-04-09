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

import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.configuration.Keys;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnexpectedChannelProbeRespAlert extends Alert {


    private static final String DESCRIPTION = "One of our stations is replying to devices that are looking for known access points (probing) on a channel that is not in " +
            "the list of configured expected channels. This could indicate that a possible attacker is not careful enough and does not limit spoofing to channels that are " +
            "in use by the legitimate access points.";
    private static final String DOC_LINK = "guidance-UNEXPECTED_CHANNEL_PROBERESP";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>() {{
        add("A legitimate change of the access point configuration took place and the nzyme configuration has not been updated.");
        add("Some access points will dynamically choose channels based on RF spectrum congestion. Always include all possibly used channels in the nzyme configuration.");
    }};

    private UnexpectedChannelProbeRespAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, Dot11Probe probe) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, probe);
    }

    @Override
    public String getMessage() {
        return "SSID [" + getSSID() + "] was advertised with a probe response frame on an unexpected channel.";
    }

    @Override
    public Type getType() {
        return Type.UNEXPECTED_CHANNEL_PROBERESP;
    }

    public String getSSID() {
        return (String) getFields().get(Keys.SSID);
    }

    public int getChannel() {
        return (int) getFields().get(Keys.CHANNEL);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof UnexpectedChannelProbeRespAlert)) {
            return false;
        }

        UnexpectedChannelProbeRespAlert a = (UnexpectedChannelProbeRespAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getChannel() == this.getChannel();
    }

    public static UnexpectedChannelProbeRespAlert create(String ssid, String bssid, Dot11MetaInformation meta, Dot11Probe probe) {
        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(Keys.SSID, ssid);
        fields.put(Keys.BSSID, bssid.toLowerCase());
        fields.put(Keys.CHANNEL, meta.getChannel());
        fields.put(Keys.FREQUENCY, meta.getFrequency());
        fields.put(Keys.ANTENNA_SIGNAL, meta.getAntennaSignal());

        return new UnexpectedChannelProbeRespAlert(DateTime.now(), Subsystem.DOT_11, fields.build(), probe);
    }

}
