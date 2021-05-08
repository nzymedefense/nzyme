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
import horse.wtf.nzyme.alerts.UnknownSSIDAlert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.networks.sentry.Sentry;
import horse.wtf.nzyme.util.Tools;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class SentryInterceptorSet {

    private final Sentry sentry;
    private final AlertsService alerts;

    public SentryInterceptorSet(Sentry sentry, AlertsService alerts) {
        this.sentry = sentry;
        this.alerts = alerts;
    }

    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        interceptors.add(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            @Override
            public void intercept(Dot11BeaconFrame frame) {
                if (frame.ssid() == null || frame.ssid().trim().isEmpty() || !Tools.isHumanlyReadable(frame.ssid())) {
                    return;
                }

                if (!sentry.knowsSSID(frame.ssid())) {
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
                return new ArrayList<Class<? extends Alert>>(){{
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

                if (!sentry.knowsSSID(frame.ssid())) {
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
                return new ArrayList<Class<? extends Alert>>(){{
                    add(UnknownSSIDAlert.class);
                }};
            }
        });

        return interceptors.build();
    }

}
