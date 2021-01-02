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

package horse.wtf.nzyme.dot11.interceptors;

import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.UnexpectedChannelBeaconAlert;
import horse.wtf.nzyme.alerts.UnexpectedChannelProbeRespAlert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.configuration.Dot11NetworkDefinition;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.ArrayList;
import java.util.List;

public class UnexpectedChannelInterceptorSet {

    private final List<Dot11NetworkDefinition> configuredNetworks;

    private final AlertsService alerts;

    public UnexpectedChannelInterceptorSet(AlertsService alerts, List<Dot11NetworkDefinition> configuredNetworks) {
        this.alerts = alerts;
        this.configuredNetworks = configuredNetworks;
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

                for (Dot11NetworkDefinition network : configuredNetworks) {
                    if (network.ssid().equals(frame.ssid()) && !network.channels().contains(frame.meta().getChannel())) {
                        alerts.handle(UnexpectedChannelProbeRespAlert.create(
                                DateTime.now(),
                                frame.ssid(),
                                frame.transmitter(),
                                frame.meta().getChannel(),
                                frame.meta().getFrequency(),
                                frame.meta().getAntennaSignal(),
                                1
                        ));
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
                    add(UnexpectedChannelProbeRespAlert.class);
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

                for (Dot11NetworkDefinition network : configuredNetworks) {
                    if (network.ssid().equals(frame.ssid()) && !network.channels().contains(frame.meta().getChannel())) {
                        alerts.handle(UnexpectedChannelBeaconAlert.create(
                                DateTime.now(),
                                frame.ssid(),
                                frame.transmitter(),
                                frame.meta().getChannel(),
                                frame.meta().getFrequency(),
                                frame.meta().getAntennaSignal(),
                                1
                        ));
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
                    add(UnexpectedChannelBeaconAlert.class);
                }};
            }
        });

        return interceptors.build();
    }


}
