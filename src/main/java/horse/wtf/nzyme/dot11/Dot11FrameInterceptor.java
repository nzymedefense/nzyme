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

package horse.wtf.nzyme.dot11;

import horse.wtf.nzyme.alerts.Alert;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.List;

public interface Dot11FrameInterceptor<T> {

    void intercept(T frame) throws IllegalRawDataException;
    byte forSubtype();
    List<Class<? extends Alert>> raisesAlerts();

}
