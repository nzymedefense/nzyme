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

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.math.Stats;
import horse.wtf.nzyme.dot11.networks.Channel;
import org.joda.time.DateTime;

import java.util.List;

public class SignalStrengthTable {

    private final Object mutex = new Object();

    private List<SignalStrength> table;

    public SignalStrengthTable(Channel channel) {
        this.table = newEmptyTable();
    }

    public void recordSignalStrength(SignalStrength signalStrength) {
        // TODO ZSCORE record mutex wait here

        synchronized (mutex) {
            table.add(signalStrength);
        }
    }

    public void retentionClean(int seconds) {
        DateTime cutoff = DateTime.now().minusSeconds(seconds);

        // TODO ZSCORE record mutex wait here.
        // TODO ZSCORE record total retention clean time here

        List<SignalStrength> newList = newEmptyTable();
        synchronized (mutex) {
            for (SignalStrength s : table) {
                if (s.timestamp().isAfter(cutoff)) {
                    newList.add(s);
                }
            }
            table = newList;
        }
    }

    // TODO ZSCORE: timer
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

    public List<SignalStrength> copyOfTable() {
        synchronized (mutex) {
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
