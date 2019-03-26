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

package horse.wtf.nzyme.dot11.interceptors;

import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.alerts.CryptoDropAlert;
import horse.wtf.nzyme.configuration.Dot11NetworkDefinition;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.List;

public class CryptoDropInterceptorSet {

    private final List<Dot11NetworkDefinition> configuredNetworks;
    private final Dot11Probe probe;

    public CryptoDropInterceptorSet(Dot11Probe probe) {
        this.probe = probe;
        this.configuredNetworks = probe.getConfiguration().getDot11Networks();
    }

    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        // React on probe-resp frames without WPA2.
        interceptors.add(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            @Override
            public void intercept(Dot11ProbeResponseFrame frame) throws IllegalRawDataException {
                // Don't consider broadcast frames.
                if (frame.ssid() == null) {
                    return;
                }

                for (Dot11NetworkDefinition network : configuredNetworks) {
                    if(network.ssid().equals(frame.ssid()) && network.WPA2() && !frame.taggedParameters().isWPA2()) {
                        probe.raiseAlert(CryptoDropAlert.create(frame.ssid(), frame.transmitter(), frame.meta(), probe));
                    }
                }
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_RESPONSE;
            }
        });

        // React on beacon frames without WPA2.
        interceptors.add(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            @Override
            public void intercept(Dot11BeaconFrame frame) throws IllegalRawDataException {
                // Don't consider broadcast frames.
                if (frame.ssid() == null) {
                    return;
                }

                for (Dot11NetworkDefinition network : configuredNetworks) {
                    if(network.ssid().equals(frame.ssid()) && network.WPA2() && !frame.taggedParameters().isWPA2()) {
                        probe.raiseAlert(CryptoDropAlert.create(frame.ssid(), frame.transmitter(), frame.meta(), probe));
                    }
                }
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.BEACON;
            }
        });

        return interceptors.build();
    }

    private void doIntercept() {

    }

}
