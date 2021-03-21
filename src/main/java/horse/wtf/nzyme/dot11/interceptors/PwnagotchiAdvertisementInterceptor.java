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
import horse.wtf.nzyme.alerts.PwnagotchiAdvertisementAlert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.misc.PwnagotchiAdvertisementExtractor;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.ArrayList;
import java.util.List;

public class PwnagotchiAdvertisementInterceptor implements Dot11FrameInterceptor<Dot11BeaconFrame> {

    private final PwnagotchiAdvertisementExtractor extractor;

    private final AlertsService alerts;

    public PwnagotchiAdvertisementInterceptor(AlertsService alerts) {
        this.alerts = alerts;
        this.extractor = new PwnagotchiAdvertisementExtractor();
    }

    @Override
    public void intercept(Dot11BeaconFrame frame) throws IllegalRawDataException {
        extractor.extract(frame).ifPresent(advertisement -> alerts.handle(PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                advertisement,
                frame.meta().getChannel(),
                frame.meta().getFrequency(),
                frame.meta().getAntennaSignal(),
                1)
        ));
    }

    @Override
    public byte forSubtype() {
        return Dot11FrameSubtype.BEACON;
    }

    @Override
    public List<Class<? extends Alert>> raisesAlerts() {
        return new ArrayList<Class<? extends Alert>>(){{
            add(PwnagotchiAdvertisementAlert.class);
        }};
    }

}
