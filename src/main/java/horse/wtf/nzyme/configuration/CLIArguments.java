package horse.wtf.nzyme.configuration;

import com.beust.jcommander.Parameter;

public class CLIArguments {

    @Parameter(names={"--config-file", "-c"}, required = true)
    private String configFilePath;

    public String getConfigFilePath() {
        return configFilePath;
    }

}
