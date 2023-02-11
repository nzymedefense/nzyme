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
import app.nzyme.core.alerts.UnknownSSIDAlert;
import app.nzyme.core.alerts.service.AlertsService;
import app.nzyme.core.dot11.Dot11FrameInterceptor;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.frames.Dot11BeaconFrame;
import app.nzyme.core.dot11.frames.Dot11ProbeResponseFrame;
import app.nzyme.core.dot11.networks.sentry.Sentry;
import app.nzyme.core.util.Tools;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class SentryInterceptorSet {

    private final Sentry sentry;
    private final AlertsService alerts;
    private final boolean unknownSSIDAlertEnabled;

    public SentryInterceptorSet(Sentry sentry, AlertsService alerts, boolean unknownSSIDAlertEnabled) {
        this.sentry = sentry;
        this.alerts = alerts;
        this.unknownSSIDAlertEnabled = unknownSSIDAlertEnabled;
    }

    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        interceptors.add(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            @Override
            public void intercept(Dot11BeaconFrame frame) {
                if (frame.ssid() == null || frame.ssid().trim().isEmpty() || !Tools.isHumanlyReadable(frame.ssid())) {
                    return;
                }

                if (unknownSSIDAlertEnabled && !sentry.knowsSSID(frame.ssid())) {
                    alerts.handle(UnknownSSIDAlert.create(
                            DateTime.now(),
                            frame.ssid(),
                            frame.transmitter(),
                            frame.meta().getChannel(),
                            frame.meta().getFrequency(),
                            frame.meta().getAntennaSignal()
                    ));
                }

                sentry.tickSSID(frame.ssid(), DateTime.now());
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.BEACON;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return new ArrayList<>() {{
                    add(UnknownSSIDAlert.class);
                }};
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            @Override
            public void intercept(Dot11ProbeResponseFrame frame) {
                if (frame.ssid() == null || frame.ssid().trim().isEmpty() || !Tools.isHumanlyReadable(frame.ssid())) {
                    return;
                }

                if (unknownSSIDAlertEnabled && !sentry.knowsSSID(frame.ssid())) {
                    alerts.handle(UnknownSSIDAlert.create(
                            DateTime.now(),
                            frame.ssid(),
                            frame.transmitter(),
                            frame.meta().getChannel(),
                            frame.meta().getFrequency(),
                            frame.meta().getAntennaSignal()
                    ));
                }

                sentry.tickSSID(frame.ssid(), DateTime.now());
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_RESPONSE;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return new ArrayList<>() {{
                    add(UnknownSSIDAlert.class);
                }};
            }
        });

        return interceptors.build();
    }

}
