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

package app.nzyme.core.dot11.interceptors;

import com.google.common.collect.ImmutableList;
import app.nzyme.core.alerts.Alert;
import app.nzyme.core.alerts.UnexpectedSSIDBeaconAlert;
import app.nzyme.core.alerts.UnexpectedSSIDProbeRespAlert;
import app.nzyme.core.alerts.service.AlertsService;
import app.nzyme.core.configuration.Dot11NetworkDefinition;
import app.nzyme.core.dot11.Dot11FrameInterceptor;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.frames.Dot11BeaconFrame;
import app.nzyme.core.dot11.frames.Dot11ProbeResponseFrame;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class UnexpectedSSIDInterceptorSet {

    private final List<Dot11NetworkDefinition> configuredNetworks;

    private final AlertsService alerts;

    public UnexpectedSSIDInterceptorSet(AlertsService alerts, List<Dot11NetworkDefinition> networks) {
        this.alerts = alerts;
        this.configuredNetworks = networks;
    }

    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        // React on probe-resp frames from one of our BSSIDs that is advertising a network that is not ours.
        interceptors.add(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            @Override
            public void intercept(Dot11ProbeResponseFrame frame) {
                // Don't consider broadcast frames.
                if (frame.ssid() == null) {
                    return;
                }

                for (Dot11NetworkDefinition network : configuredNetworks) {
                    if (network.allBSSIDAddresses().contains(frame.transmitter()) && !network.ssid().equals(frame.ssid())) {
                        alerts.handle(UnexpectedSSIDProbeRespAlert.create(
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
                return new ArrayList<Class<? extends Alert>>(){{
                    add(UnexpectedSSIDProbeRespAlert.class);
                }};
            }
        });

        // React on beacon frames from one of our BSSIDs that is advertising a network that is not ours.
        interceptors.add(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            @Override
            public void intercept(Dot11BeaconFrame frame) {
                // Don't consider broadcast frames.
                if (frame.ssid() == null) {
                    return;
                }

                for (Dot11NetworkDefinition network : configuredNetworks) {
                    if (network.allBSSIDAddresses().contains(frame.transmitter()) && !network.ssid().equals(frame.ssid())) {
                        alerts.handle(UnexpectedSSIDBeaconAlert.create(
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
                return new ArrayList<Class<? extends Alert>>(){{
                    add(UnexpectedSSIDBeaconAlert.class);
                }};
            }
        });

        return interceptors.build();
    }

}
