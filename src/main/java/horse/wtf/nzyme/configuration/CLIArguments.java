package horse.wtf.nzyme.configuration;

import com.beust.jcommander.Parameter;
import horse.wtf.nzyme.graylog.GraylogAddress;

public class CLIArguments {

    @Parameter(names={"--interface", "-i"}, required = true)
    private String networkInterface;

    @Parameter(names={"--graylog", "-g"}, required = true, validateValueWith = InternetAddressValidator.class)
    private String graylogAddress;

    @Parameter(names={"--beacon-sampling", "-s"})
    private int beaconSamplingRate = 0;

    public String getNetworkInterface() {
        return networkInterface;
    }

    public GraylogAddress getGraylogAddress() {
        String[] parts = graylogAddress.split(":");
        return new GraylogAddress(parts[0], Integer.parseInt(parts[1]));
    }

    public int getBeaconSamplingRate() {
        return beaconSamplingRate;
    }

}
