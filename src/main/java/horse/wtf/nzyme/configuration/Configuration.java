package horse.wtf.nzyme.configuration;

import com.github.joschi.jadconfig.Parameter;
import com.github.joschi.jadconfig.validators.PositiveIntegerValidator;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.graylog.GraylogAddress;

public class Configuration {

    @Parameter(value = "interface", required = true)
    private String networkInterface;

    @Parameter(value = "graylog_address", validator = InternetAddressValidator.class, required = true)
    private String graylogAddress;

    @Parameter(value = "channels", validator = SplittableListValidator.class, required = true)
    private String channels;

    @Parameter(value = "channel_hop_command", required = true)
    private String channelHopCommand;

    @Parameter(value = "channel_hop_interval", validator = PositiveIntegerValidator.class, required = true)
    private int channelHopInterval;

    @Parameter(value = "beacon_frame_sampling_rate", validator = PositiveIntegerValidator.class, required = true)
    private int beaconSamplingRate;

    public GraylogAddress getGraylogAddress() {
        String[] parts = graylogAddress.split(":");
        return new GraylogAddress(parts[0], Integer.parseInt(parts[1]));
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
