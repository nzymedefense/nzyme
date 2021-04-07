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
import horse.wtf.nzyme.alerts.BeaconTrapResponseAlert;
import horse.wtf.nzyme.alerts.ProbeRequestTrapResponseAlert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.ArrayList;
import java.util.List;

public class BeaconTrapResponseInterceptorSet {

    private static final Logger LOG = LogManager.getLogger(BeaconTrapResponseInterceptorSet.class);

    private final List<String> trappedSSIDs;
    private final String ourFingerprint;

    private final AlertsService alerts;

    public BeaconTrapResponseInterceptorSet(AlertsService alerts, List<String> trappedSSIDs, String ourFingerprint) {
        this.alerts = alerts;
        this.trappedSSIDs = trappedSSIDs;
        this.ourFingerprint = ourFingerprint;
    }

    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        interceptors.add(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            @Override
            public void intercept(Dot11ProbeResponseFrame frame) {
                // Don't consider broadcast frames.
                if (frame.ssid() == null) {
                    return;
                }

                for (String ssid : trappedSSIDs) {
                    if (ssid.equals(frame.ssid())) {
                        alerts.handle(BeaconTrapResponseAlert.create(
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

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_RESPONSE;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return new ArrayList<>() {{
                    add(ProbeRequestTrapResponseAlert.class);
                }};
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            @Override
            public void intercept(Dot11BeaconFrame frame) {
                // Don't consider broadcast frames.
                if (frame.ssid() == null) {
                    return;
                }

                if (frame.transmitterFingerprint().equals(ourFingerprint)) {
                    LOG.trace("Skipping frame with our own fingerprint [{}]", ourFingerprint);
                    return;
                }

                for (String ssid : trappedSSIDs) {
                    if (ssid.equals(frame.ssid())) {
                        alerts.handle(BeaconTrapResponseAlert.create(
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

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.BEACON;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return new ArrayList<>() {{
                    add(BeaconTrapResponseAlert.class);
                }};
            }
        });

        return interceptors.build();
    }

}
