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

package horse.wtf.nzyme.dot11.networks;

import com.google.common.collect.Maps;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.frames.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Networks {

    /*
     * TODO keep a graph of who usually sends deauths for whom and with what signal strength
     */

    private static final Logger LOG = LogManager.getLogger(Networks.class);

    private final Map<String, BSSID> bssids;
    private final Nzyme nzyme;

    public Networks(Nzyme nzyme) {
        this.nzyme = nzyme;
        this.bssids = Maps.newHashMap();
    }

    public void registerBeaconFrame(Dot11BeaconFrame frame) {
        register(frame.transmitter(), frame.ssid(), frame.meta().getChannel(), frame.meta().getSignalQuality());
    }

    public void registerProbeResponseFrame(Dot11ProbeResponseFrame frame) {
        register(frame.transmitter(), frame.ssid(), frame.meta().getChannel(), frame.meta().getSignalQuality());
    }

    private synchronized void register(String transmitter, String ssidName, int channelNumber, int signalQuality) {
        // Ensure that the BSSID exists in the map.
        BSSID bssid;
        if (bssids.containsKey(transmitter)) {
            bssid = bssids.get(transmitter);

            // Ensure that the SSID has been recorded for this BSSID.
            if(!bssid.ssids().containsKey(ssidName)) {
                bssid.ssids().put(ssidName, SSID.create(ssidName));
            }
        } else {
            // First time we are seeing this BSSID.
            String oui = nzyme.getOUIManager().lookupBSSID(transmitter);

            if (oui == null) {
                oui = "unknown";
            }

            SSID ssid = SSID.create(ssidName);
            bssid = BSSID.create(new HashMap<String, SSID>(){{
                put(ssidName, ssid);
            }}, oui);

            bssids.put(transmitter, bssid);
        }

        // Find our SSID.
        SSID ssid = bssid.ssids().get(ssidName);

        try {
            // Create or update channel.
            if (ssid.channels().containsKey(channelNumber)) {
                // Update channel statistics.
                Channel channel = ssid.channels().get(channelNumber);
                channel.totalFrames().incrementAndGet();

                // Add signal quality to history of signal qualities.
                channel.recentSignalQuality().add(signalQuality);

                // Update max or min quality if required.
                boolean inDelta = true;
                if (signalQuality > channel.signalMax().get()) {
                    channel.signalMax().set(signalQuality);
                    inDelta = false;
                }
                if (signalQuality < channel.signalMin().get()) {
                    channel.signalMin().set(signalQuality);
                    inDelta = false;
                }

                // Add delta state.
                channel.recentDeltaStates().add(inDelta);

                ssid.channels().replace(channelNumber, channel);
            } else {
                // Create new channel.
                Channel channel = Channel.create(new AtomicLong(1), signalQuality);
                ssid.channels().put(channelNumber, channel);
            }
        } catch (NullPointerException e) {
            LOG.error(ssid);
            throw e;
        }

    }

    public Map<String, BSSID> getBSSIDs() {
        return bssids;
    }

    public SignalDelta getSignalDelta(String bssid, String ssidName, int channelNumber) throws NoSuchNetworkException, NoSuchChannelException {
        if(!getBSSIDs().containsKey(bssid) || !getBSSIDs().get(bssid).ssids().containsKey(ssidName)) {
            throw new NoSuchNetworkException();
        }

        SSID ssid = getBSSIDs().get(bssid).ssids().get(ssidName);

        if(!ssid.channels().containsKey(channelNumber)) {
            throw new NoSuchChannelException();
        }

        return ssid.channels().get(channelNumber).expectedDelta();
    }

    public static class NoSuchNetworkException extends Exception {
    }

    public static class NoSuchChannelException extends Exception {
    }
}
