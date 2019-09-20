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

package horse.wtf.nzyme.dot11.networks.signalstrength;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.util.MetricNames;
import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SignalStrengthTable {

    private final Object mutex = new Object();

    private List<SignalStrength> table;

    private final Timer mutexTimer;

    public SignalStrengthTable(MetricRegistry metrics) {
        this.table = newEmptyTable();

        this.mutexTimer = metrics.timer(MetricNames.SIGNAL_TABLES_MUTEX_WAIT);
    }

    public void recordSignalStrength(SignalStrength signalStrength) {
        Timer.Context timer = mutexTimer.time();
         synchronized (mutex) {
             timer.stop();
             table.add(signalStrength);
         }
    }

    public void retentionClean(int seconds) {
        DateTime cutoff = DateTime.now().minusSeconds(seconds);

        // TODO ZSCORE record total retention clean time here

        List<SignalStrength> newList = newEmptyTable();

        Timer.Context timer = mutexTimer.time();
        synchronized (mutex) {
            timer.stop();
            for (SignalStrength s : table) {
                if (s.timestamp().isAfter(cutoff)) {
                    newList.add(s);
                }
            }
            table = newList;
        }
    }

    public int getSize() {
        return table.size();
    }

    public Map<Integer, AtomicLong> getSignalDistributionHistogram() {
        Map<Integer, AtomicLong> histogram = Maps.newTreeMap();

        for (SignalStrength signalStrength : copyOfTable()) {
            if (histogram.containsKey(signalStrength.signalStrength())) {
                histogram.get(signalStrength.signalStrength()).incrementAndGet();
            } else {
                histogram.put(signalStrength.signalStrength(), new AtomicLong(1));
            }
        }

        return histogram;
    }

    public int getBestSiqnalStrength() {
        return copyOfAllValues()
                .stream()
                .min(Comparator.comparingInt(i -> i))
                .orElse(0);
    }

    private List<Integer> copyOfAllValues() {
        List<SignalStrength> copy = copyOfTable();
        List<Integer> values = Lists.newArrayList();

        for (SignalStrength s : copy) {
            values.add(s.signalStrength());
        }

        return values;
    }

    private List<SignalStrength> newEmptyTable() {
        return Lists.newArrayList();
    }

    private List<SignalStrength> copyOfTable() {
        Timer.Context timer = mutexTimer.time();
        synchronized (mutex) {
            timer.stop();
            List<SignalStrength> copy = newEmptyTable();
            copy.addAll(table);
            return copy;
        }
    }

    @AutoValue
    public static abstract class SignalStrength {

        public abstract DateTime timestamp();
        public abstract Integer signalStrength();

        public static SignalStrength create(DateTime timestamp, Integer signalStrength) {
            return builder()
                    .timestamp(timestamp)
                    .signalStrength(signalStrength)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_SignalStrengthTable_SignalStrength.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder timestamp(DateTime timestamp);

            public abstract Builder signalStrength(Integer signalStrength);

            public abstract SignalStrength build();
        }

    }

}
