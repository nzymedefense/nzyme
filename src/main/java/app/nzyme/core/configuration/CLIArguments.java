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

package app.nzyme.core.configuration;

import com.beust.jcommander.Parameter;

public class CLIArguments {

    @Parameter(names={"--config-file", "-c"}, required = true)
    private String configFilePath;

    @Parameter(names={"--debug", "-d"})
    private boolean debugMode;

    @Parameter(names={"--trace", "-t"})
    private boolean traceMode;

    @Parameter(names={"--bootstrap-test", "-b"})
    private boolean bootstrapTestMode;

    @Parameter(names={"--no-pid-check", "-p"})
    private boolean noPidCheckMode;

    public String getConfigFilePath() {
        return configFilePath;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isTraceMode() {
        return traceMode;
    }

    public boolean isBootstrapTestMode() {
        return bootstrapTestMode;
    }

    public boolean isNoPidCheckMode() {
        return noPidCheckMode;
    }

}
