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

package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import org.pcap4j.packet.IllegalRawDataException;

import static com.codahale.metrics.MetricRegistry.name;

public abstract class Dot11FrameParser<T> {

    protected final MetricRegistry metrics;

    private final Timer timer;

    protected Dot11FrameParser(MetricRegistry metrics) {
        this.metrics = metrics;

        this.timer = metrics.timer(name(this.getClass(), "parseTime"));
    }

    protected abstract T doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException;

    public T parse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Timer.Context time = this.timer.time();

        T result;
        try {
            result = doParse(payload, header, meta);
        } finally {
            time.stop();
        }

        return result;
    }

}
