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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.configuration.InvalidConfigurationException;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.deception.bluffs.Beacon;
import horse.wtf.nzyme.dot11.interceptors.BeaconTrapResponseInterceptorSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class BeaconTrap extends Trap {

    private static final Logger LOG = LogManager.getLogger(BeaconTrap.class);

    private final String interfaceName;
    private final List<String> ssids;
    private final String transmitter;
    private final int delayMilliseconds;
    private final String ourFingerprint;

    private final NzymeLeader nzyme;

    private final int framesPerExecution;

    public BeaconTrap(NzymeLeader nzyme, String interfaceName, List<String> ssids, String transmitter, int delayMilliseconds, String ourFingerprint) {
        this.nzyme = nzyme;

        this.interfaceName = interfaceName;
        this.ssids = ssids;
        this.transmitter = transmitter;
        this.delayMilliseconds = delayMilliseconds;
        this.ourFingerprint = ourFingerprint;
        this.framesPerExecution = ssids.size();

        nzyme.registerIgnoredFingerprint(ourFingerprint);
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

        if (delayMilliseconds <= 0) {
            throw new InvalidConfigurationException("Delay milliseconds must be configured to a value larger than 0.");
        }

        if (Strings.isNullOrEmpty(ourFingerprint)) {
            throw new InvalidConfigurationException("Fingerprint is empty.");
        }
    }

    @Override
    protected boolean doRun() {
        for (String ssid : ssids) {
            LOG.debug("Setting BeaconTrap for SSID [{}].", ssid);
            try {
                new Beacon(nzyme.getConfiguration(), interfaceName, ssid, transmitter).execute();
                return true;
            } catch(Exception e){
                LOG.error("Could not set Beacon for SSID [{}].", ssid, e);
                return false;
            }
        }

        return false;
    }

    @Override
    public int getDelayMilliseconds() {
        return delayMilliseconds;
    }

    @Override
    public int framesPerExecution() {
        return framesPerExecution;
    }

    @Override
    public Type getType() {
        return Type.BEACON_1;
    }

    @Override
    public String getDescription() {
        return "Setting trap for SSIDs [" + Joiner.on(", ").join(ssids) + "] every " + getDelayMilliseconds() + "ms.";
    }

    @Override
    public List<Dot11FrameInterceptor> requestedInterceptors() {
        return new BeaconTrapResponseInterceptorSet(
                nzyme.getAlertsService(),
                this.ssids,
                ourFingerprint
        ).getInterceptors();
    }


}
