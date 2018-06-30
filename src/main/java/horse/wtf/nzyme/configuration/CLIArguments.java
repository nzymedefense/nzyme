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

package horse.wtf.nzyme.configuration;

import com.beust.jcommander.Parameter;

public class CLIArguments {

    @Parameter(names={"--config-file", "-c"}, required = true)
    private String configFilePath;

    @Parameter(names={"--debug", "-d"})
    private boolean debugMode;

    @Parameter(names={"--trace", "-t"})
    private boolean traceMode;

    @Parameter(names={"--packet-info", "-p"})
    private boolean packetInfo;

    public String getConfigFilePath() {
        return configFilePath;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isTraceMode() {
        return traceMode;
    }

    public boolean isPacketInfo() {
        return packetInfo;
    }

}
