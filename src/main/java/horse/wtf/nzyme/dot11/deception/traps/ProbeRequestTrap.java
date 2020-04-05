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

package horse.wtf.nzyme.dot11.deception.traps;

import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.dot11.deception.bluffs.ProbeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ProbeRequestTrap extends Trap {

    private static final Logger LOG = LogManager.getLogger(ProbeRequestTrap.class);

    private final String interfaceName;
    private final List<String> ssids;
    private final String transmitter;
    private final int delaySeconds;
    private final int framesPerExecution;

    private final LeaderConfiguration configuration;

    public ProbeRequestTrap(LeaderConfiguration configuration, String interfaceName, List<String> ssids, String transmitter, int delaySeconds) {
        this.interfaceName = interfaceName;
        this.ssids = ssids;
        this.transmitter = transmitter;
        this.configuration = configuration;
        this.delaySeconds = delaySeconds;
        this.framesPerExecution = ssids.size();
    }

    @Override
    protected void doRun() {
        for (String ssid : ssids) {
            LOG.debug("Setting ProbeRequestTrap for SSID [{}].", ssid);
            try {
                new ProbeRequest(configuration, interfaceName, ssid, transmitter).executeFailFast();
            } catch(Exception e){
                LOG.error("Could not set ProbeRequestTrap for SSID [{}].", ssid, e);
            }
        }
    }

    @Override
    public int getDelaySeconds() {
        return delaySeconds;
    }

    @Override
    public int framesPerExecution() {
        return framesPerExecution;
    }

}
