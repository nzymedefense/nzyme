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

import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.PwnagotchiAdvertisementAlert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.misc.PwnagotchiAdvertisementExtractor;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;

import java.util.ArrayList;
import java.util.List;

public class PwnagotchiAdvertisementInterceptor implements Dot11FrameInterceptor<Dot11BeaconFrame> {

    private final Dot11Probe probe;

    private final PwnagotchiAdvertisementExtractor extractor;

    public PwnagotchiAdvertisementInterceptor(Dot11Probe probe) {
        this.probe = probe;
        this.extractor = new PwnagotchiAdvertisementExtractor();
    }

    @Override
    public void intercept(Dot11BeaconFrame frame) throws IllegalRawDataException {
        extractor.extract(frame).ifPresent(advertisement -> probe.raiseAlert(PwnagotchiAdvertisementAlert.create(
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
