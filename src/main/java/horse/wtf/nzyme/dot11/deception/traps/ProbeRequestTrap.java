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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.NzymeLeaderImpl;
import horse.wtf.nzyme.configuration.InvalidConfigurationException;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.deception.bluffs.ProbeRequest;
import horse.wtf.nzyme.dot11.interceptors.ProbeRequestTrapResponseInterceptorSet;
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

    private final NzymeLeader nzyme;

    public ProbeRequestTrap(NzymeLeader nzyme, String interfaceName, List<String> ssids, String transmitter, int delaySeconds) {
        this.nzyme = nzyme;

        this.interfaceName = interfaceName;
        this.ssids = ssids;
        this.transmitter = transmitter;
        this.delaySeconds = delaySeconds;
        this.framesPerExecution = ssids.size();
    }

    @Override
    public void checkConfiguration() throws InvalidConfigurationException {
        if (Strings.isNullOrEmpty(interfaceName)) {
            throw new InvalidConfigurationException("Interface name is empty.");
        }

        if (ssids == null || ssids.isEmpty()) {
            throw new InvalidConfigurationException("SSIDs is null or empty.");
        }

        if (Strings.isNullOrEmpty(transmitter)) {
            // TODO also check if valid MAC address
            throw new InvalidConfigurationException("Transmitter is null or emtpy.");
        }

        if (delaySeconds <= 0) {
            throw new InvalidConfigurationException("Delay seconds must be configured to a value larger than 0.");
        }

        if (framesPerExecution <= 0) {
            throw new InvalidConfigurationException("Frames per execution must be configured to a value larger than 0.");
        }
    }

    @Override
    protected void doRun() {
        for (String ssid : ssids) {
            LOG.debug("Setting ProbeRequestTrap for SSID [{}].", ssid);
            try {
                new ProbeRequest(nzyme.getConfiguration(), interfaceName, ssid, transmitter).executeFailFast();
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

    @Override
    public List<Dot11FrameInterceptor> requestedInterceptors() {
        return new ProbeRequestTrapResponseInterceptorSet(
                nzyme.getAlertsService(),
                nzyme.getConfiguration().dot11TrapDevices()
        ).getInterceptors();
    }

}
