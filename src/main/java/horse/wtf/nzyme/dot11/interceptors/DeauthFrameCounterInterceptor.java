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

import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.deauth.DeauthenticationMonitor;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;

import java.util.Collections;
import java.util.List;

public class DeauthFrameCounterInterceptor implements Dot11FrameInterceptor<Dot11DeauthenticationFrame> {

    private final DeauthenticationMonitor monitor;

    public DeauthFrameCounterInterceptor(DeauthenticationMonitor monitor) {
        this.monitor = monitor;
    }

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
        return Collections.emptyList();
    }

}
