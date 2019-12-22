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
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.KnownBanditFingerprintBeaconAlert;
import horse.wtf.nzyme.alerts.KnownBanditFingerprintProbeRespAlert;
import horse.wtf.nzyme.configuration.BanditFingerprintDefinition;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KnownBanditFingerprintInterceptorSet {

    private final Map<String, BanditFingerprintDefinition> bandits;
    private final Dot11Probe probe;

    public KnownBanditFingerprintInterceptorSet(Dot11Probe probe) {
        this.probe = probe;
        this.bandits = probe.getConfiguration().getBanditFingerprints();
    }

    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        interceptors.add(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            @Override
            public void intercept(Dot11ProbeResponseFrame frame) throws IllegalRawDataException {
                if (bandits.containsKey(frame.transmitterFingerprint())) {
                    BanditFingerprintDefinition bandit = bandits.get(frame.transmitterFingerprint());

                    probe.raiseAlert(KnownBanditFingerprintProbeRespAlert.create(
                            DateTime.now(),
                            bandit.names(),
                            bandit.fingerprint(),
                            frame.ssid() == null ? "[hidden]" : frame.ssid(),
                            frame.transmitter(),
                            frame.meta().getChannel(),
                            frame.meta().getFrequency(),
                            frame.meta().getAntennaSignal(),
                            1
                    ));
                }
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_RESPONSE;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return new ArrayList<Class<? extends Alert>>(){{
                    add(KnownBanditFingerprintProbeRespAlert.class);
                }};
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            @Override
            public void intercept(Dot11BeaconFrame frame) throws IllegalRawDataException {
                if (bandits.containsKey(frame.transmitterFingerprint())) {
                    BanditFingerprintDefinition bandit = bandits.get(frame.transmitterFingerprint());

                    probe.raiseAlert(KnownBanditFingerprintBeaconAlert.create(
                            DateTime.now(),
                            bandit.names(),
                            bandit.fingerprint(),
                            frame.ssid() == null ? "[hidden]" : frame.ssid(),
                            frame.transmitter(),
                            frame.meta().getChannel(),
                            frame.meta().getFrequency(),
                            frame.meta().getAntennaSignal(),
                            1
                    ));
                }
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.BEACON;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return new ArrayList<Class<? extends Alert>>(){{
                    add(KnownBanditFingerprintBeaconAlert.class);
                }};
            }
        });

        return interceptors.build();
    }

}
