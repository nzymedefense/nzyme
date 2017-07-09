package horse.wtf.nzyme.configuration;

import com.beust.jcommander.Parameter;

public class CLIArguments {

    @Parameter(names={"--config-file", "-c"}, required = true)
    private String configFilePath;

    @Parameter(names={"--debug", "-d"})
    private boolean debugMode;

    @Parameter(names={"--trace", "-t"})
    private boolean traceMode;

    public String getConfigFilePath() {
        return configFilePath;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isTraceMode() {
        return traceMode;
    }

}
