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
import horse.wtf.nzyme.alerts.ProbeRequestTrapResponseAlert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.configuration.ConfigurationKeys;
import horse.wtf.nzyme.configuration.Dot11TrapConfiguration;
import horse.wtf.nzyme.configuration.Dot11TrapDeviceDefinition;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.ArrayList;
import java.util.List;

public class ProbeRequestTrapResponseInterceptorSet {

    private final List<Dot11TrapDeviceDefinition> configuredTraps;

    private final AlertsService alerts;

    public ProbeRequestTrapResponseInterceptorSet(AlertsService alerts, List<Dot11TrapDeviceDefinition> configuredTraps) {
        this.alerts = alerts;
        this.configuredTraps = configuredTraps;
    }

    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        interceptors.add(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            @Override
            public void intercept(Dot11ProbeResponseFrame frame) throws IllegalRawDataException {
                // Don't consider broadcast frames.
                if (frame.ssid() == null) {
                    return;
                }

                for (Dot11TrapDeviceDefinition c : configuredTraps) {
                    for (Dot11TrapConfiguration trap : c.traps()) {
                        List<String> ssids = trap.configuration().getStringList(ConfigurationKeys.SSIDS);
                        for (String ssid : ssids) {
                            if (ssid.equals(frame.ssid())) {
                                alerts.handle(ProbeRequestTrapResponseAlert.create(
                                        DateTime.now(),
                                        ssid,
                                        frame.transmitter(),
                                        frame.meta().getChannel(),
                                        frame.meta().getFrequency(),
                                        frame.meta().getAntennaSignal(),
                                        1
                                ));
                            }
                        }
                    }
                }
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_RESPONSE;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return new ArrayList<Class<? extends Alert>>() {{
                    add(ProbeRequestTrapResponseAlert.class);
                }};
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            @Override
            public void intercept(Dot11BeaconFrame frame) throws IllegalRawDataException {
                // Don't consider broadcast frames.
                if (frame.ssid() == null) {
                    return;
                }

                for (Dot11TrapDeviceDefinition c : configuredTraps) {
                    for (Dot11TrapConfiguration trap : c.traps()) {
                        List<String> ssids = trap.configuration().getStringList(ConfigurationKeys.SSIDS);
                        for (String ssid : ssids) {
                            if (ssid.equals(frame.ssid())) {
                                alerts.handle(ProbeRequestTrapResponseAlert.create(
                                        DateTime.now(),
                                        ssid,
                                        frame.transmitter(),
                                        frame.meta().getChannel(),
                                        frame.meta().getFrequency(),
                                        frame.meta().getAntennaSignal(),
                                        1
                                ));
                            }
                        }
                    }
                }
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.BEACON;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return new ArrayList<Class<? extends Alert>>() {{
                    add(ProbeRequestTrapResponseAlert.class);
                }};
            }
        });

        return interceptors.build();
    }

}
