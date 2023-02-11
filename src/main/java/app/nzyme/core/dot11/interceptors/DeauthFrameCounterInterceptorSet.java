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
import app.nzyme.core.dot11.Dot11FrameInterceptor;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.deauth.DeauthenticationMonitor;
import app.nzyme.core.dot11.frames.Dot11DeauthenticationFrame;
import app.nzyme.core.dot11.frames.Dot11DisassociationFrame;

import java.util.Collections;
import java.util.List;

public class DeauthFrameCounterInterceptorSet {

    private final DeauthenticationMonitor monitor;

    public DeauthFrameCounterInterceptorSet(DeauthenticationMonitor monitor) {
        this.monitor = monitor;
    }


    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        interceptors.add(new Dot11FrameInterceptor<Dot11DeauthenticationFrame>() {
            @Override
            public void intercept(Dot11DeauthenticationFrame frame) {
                monitor.record(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.DEAUTHENTICATION;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.EMPTY_LIST;
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11DisassociationFrame>() {
            @Override
            public void intercept(Dot11DisassociationFrame frame) {
                monitor.record(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.DISASSOCIATION;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.EMPTY_LIST;
            }
        });

        return interceptors.build();
    }

}
