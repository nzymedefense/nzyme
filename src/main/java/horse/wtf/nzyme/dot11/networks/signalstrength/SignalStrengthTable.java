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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.math.Stats;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.util.MetricNames;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public double calculateZScore(int from) {
        List<Integer> values = copyOfAllValues();
        if (values.isEmpty()) {
            return 0;
        }

        Stats stats = Stats.of(values);
        Double result = (from-stats.mean())/stats.populationStandardDeviation();

        if (result.isNaN() || result.isInfinite()) {
            return 0;
        }

        return result;
    }

    public int getSize() {
        return table.size();
    }

    public Map<Double, AtomicLong> getZScoreDistributionHistogram() {
        Map<Double, AtomicLong> histogram = Maps.newTreeMap();

        for (SignalStrength signalStrength : copyOfTable()) {
            double roundedZScore = round(signalStrength.zScore(), 1);

            if (histogram.containsKey(roundedZScore)) {
                histogram.get(roundedZScore).incrementAndGet();
            } else {
                histogram.put(roundedZScore, new AtomicLong(1));
            }
        }

        return histogram;
    }

    public int getBestSiqnalQuality() {
        return copyOfAllValues()
                .stream()
                .max(Comparator.comparingInt(i -> i))
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

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @AutoValue
    public static abstract class SignalStrength {

        public abstract DateTime timestamp();
        public abstract Integer signalStrength();
        public abstract Double zScore();

        public static SignalStrength create(DateTime timestamp, Integer signalStrength, Double zScore) {
            return builder()
                    .timestamp(timestamp)
                    .signalStrength(signalStrength)
                    .zScore(zScore)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_SignalStrengthTable_SignalStrength.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder timestamp(DateTime timestamp);

            public abstract Builder signalStrength(Integer signalStrength);

            public abstract Builder zScore(Double zScore);

            public abstract SignalStrength build();
        }

    }

}
