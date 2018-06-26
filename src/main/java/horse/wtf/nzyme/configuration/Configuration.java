/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.configuration;

import com.beust.jcommander.internal.Lists;
import com.github.joschi.jadconfig.Parameter;
import com.github.joschi.jadconfig.validators.PositiveIntegerValidator;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import horse.wtf.nzyme.util.Tools;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.List;

public class Configuration {

    @Parameter(value = "nzyme_id", required = true)
    protected String nzymeId;

    @Parameter(value = "graylog_addresses", validator = InternetAddressValidator.class, required = false)
    protected String graylogAddresses;

    @Parameter(value = "channels", validator = InterfacesAndChannelsValidator.class, required = true)
    protected String channels;

    @Parameter(value = "channel_hop_command", required = true)
    protected String channelHopCommand;

    @Parameter(value = "channel_hop_interval", validator = PositiveIntegerValidator.class, required = true)
    protected int channelHopInterval;

    @Parameter(value = "beacon_frame_sampling_rate", validator = PositiveIntegerValidator.class, required = true)
    protected int beaconSamplingRate;

    @Parameter(value = "versionchecks_enabled")
    protected boolean versionchecksEnabled = true;

    @Parameter(value = "python", required = true)
    protected String python;

    @Parameter(value = "bluff_directory", required = true)
    protected String bluffDirectory = "/tmp";

    @Parameter(value = "bluff_prefix", required = true)
    protected String bluffPrefix = "nzyme_";

    public String getNzymeId() {
        return nzymeId;
    }

    @Nullable
    public List<GraylogAddress> getGraylogAddresses() {
        if(graylogAddresses == null) {
            return null;
        }

        String[] addresses;

        if (graylogAddresses.contains(",")) {
            addresses = graylogAddresses.split(",");
        } else {
            addresses = new String[]{graylogAddresses};
        }

        List<GraylogAddress> result = Lists.newArrayList();
        for (String address : addresses) {
            String[] parts = address.split(":");
            result.add(new GraylogAddress(parts[0], Integer.parseInt(parts[1])));
        }

        return result;
    }

    public int getBeaconSamplingRate() {
        return beaconSamplingRate;
    }

    public ImmutableMap<String, ImmutableList<Integer>> getChannels() {
        ImmutableMap.Builder<String, ImmutableList<Integer>> result = new ImmutableMap.Builder<>();

        if(!channels.contains("|")) {
            // Only one interface specified.
            result.put(parseInterfaceAndChannels(channels));
        } else {
            for (String interfaceInfo : Splitter.on("|").split(channels)) {
                result.put(parseInterfaceAndChannels(interfaceInfo));
            }
        }

        return result.build();
    }

    private AbstractMap.SimpleEntry<String, ImmutableList<Integer>> parseInterfaceAndChannels(String interfaceInfo) {
        List<String> parts = Splitter.on(":").splitToList(interfaceInfo);
        String interfaceName = parts.get(0);

        ImmutableList.Builder<Integer> channels = new ImmutableList.Builder<>();
        if(!parts.get(1).contains(",")) {
            // Only one channel specified.
            channels.add(Integer.valueOf(parts.get(1)));
        } else {
            for (String channel : Splitter.on(",").split(parts.get(1))) {
                channels.add(Integer.valueOf(channel));
            }
        }

        return new AbstractMap.SimpleEntry<>(interfaceName, channels.build());
    }

    public String getChannelHopCommand() {
        return channelHopCommand;
    }

    public int getChannelHopInterval() {
        return channelHopInterval;
    }

    public boolean areVersionchecksEnabled() {
        return versionchecksEnabled;
    }

    public String getPython() {
        return python.trim();
    }

    public String getBluffDirectory() {
        String fixedBluffDirectory;

        if (bluffDirectory.startsWith("/")) {
            fixedBluffDirectory = bluffDirectory;
        } else {
            fixedBluffDirectory = "/" + bluffDirectory;
        }

        return fixedBluffDirectory.trim();
    }

    public String getBluffPrefix() {
        return bluffPrefix.trim();
    }

}
