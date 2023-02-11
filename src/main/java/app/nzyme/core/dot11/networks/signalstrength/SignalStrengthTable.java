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

package app.nzyme.core.dot11.networks.signalstrength;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.util.MetricNames;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SignalStrengthTable {

    public static final int RETENTION_MINUTES = 5;

    private final Object mutex = new Object();

    private List<SignalStrength> table;

    private final String bssid;
    private final String ssid;
    private final int channelNumber;

    private final Timer mutexTimer;

    public SignalStrengthTable(String bssid, String ssid, int channelNumber, MetricRegistry metrics) {
        this.table = newEmptyTable();

        this.bssid = bssid;
        this.ssid = ssid;
        this.channelNumber = channelNumber;

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

    public List<Integer> copyOfAllValues() {
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
