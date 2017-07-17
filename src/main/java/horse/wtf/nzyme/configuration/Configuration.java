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
import horse.wtf.nzyme.graylog.GraylogAddress;

import java.util.List;

public class Configuration {

    @Parameter(value = "nzyme_id", required = true)
    protected String nzymeId;

    @Parameter(value = "interface", required = true)
    protected String networkInterface;

    @Parameter(value = "graylog_addresses", validator = InternetAddressValidator.class, required = true)
    protected String graylogAddresses;

    @Parameter(value = "channels", validator = SplittableListValidator.class, required = true)
    protected String channels;

    @Parameter(value = "channel_hop_command", required = true)
    protected String channelHopCommand;

    @Parameter(value = "channel_hop_interval", validator = PositiveIntegerValidator.class, required = true)
    protected int channelHopInterval;

    @Parameter(value = "beacon_frame_sampling_rate", validator = PositiveIntegerValidator.class, required = true)
    protected int beaconSamplingRate;

    public String getNzymeId() {
        return nzymeId;
    }

    public List<GraylogAddress> getGraylogAddresses() {
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

    public String getNetworkInterface() {
        return networkInterface;
    }

    public int getBeaconSamplingRate() {
        return beaconSamplingRate;
    }

    public ImmutableList<Integer> getChannels() {
        ImmutableList.Builder<Integer> builder = new ImmutableList.Builder<>();

        for (String s : Splitter.on(",").omitEmptyStrings().split(channels)) {
            builder.add(Integer.valueOf(s));
        }

        return builder.build();
    }

    public String getChannelHopCommand() {
        return channelHopCommand;
    }

    public int getChannelHopInterval() {
        return channelHopInterval;
    }

}
