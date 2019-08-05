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

package horse.wtf.nzyme.util;

import horse.wtf.nzyme.dot11.Dot11TaggedParameters;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.dot11.networks.sigindex.SignalIndexManager;
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.periodicals.alerting.beaconrate.BeaconRateAnomalyAlertMonitor;
import horse.wtf.nzyme.periodicals.alerting.sigindex.SignalIndexAnomalyAlertMonitor;
import horse.wtf.nzyme.periodicals.alerting.sigindex.SignalIndexCleaner;
import horse.wtf.nzyme.periodicals.alerting.sigindex.SignalIndexWriter;

import static com.codahale.metrics.MetricRegistry.name;

public class MetricNames {

    // Standard/provided metrics.
    public static final String MEMORY_HEAP_INIT = "mem.heap.init";
    public static final String MEMORY_HEAP_MAX = "mem.heap.max";
    public static final String MEMORY_HEAP_USAGE_PERCENT = "mem.heap.usage";
    public static final String MEMORY_HEAP_USED = "mem.heap.used";
    public static final String MEMORY_NONHEAP_INIT = "mem.non-heap.init";
    public static final String MEMORY_NONHEAP_MAX = "mem.non-heap.max";
    public static final String MEMORY_NONHEAP_USAGE_PERCENT = "mem.non-heap.usage";
    public static final String MEMORY_NONHEAP_USED = "mem.non-heap.used";

    // Custom.
    public static final String FRAME_COUNT = name(Dot11MonitorProbe.class, "frames");
    public static final String FRAME_TIMER = name(Dot11MonitorProbe.class, "timing");
    public static final String OUI_LOOKUP_TIMER = name(OUIManager.class, "lookup-timing");
    public static final String TAGGED_PARAMS_PARSE_TIMER = name(Dot11TaggedParameters.class, "parse-timing");
    public static final String TAGGED_PARAMS_FINGERPRINT_TIMER = name(Dot11TaggedParameters.class, "fingerprint-timing");
    public static final String NETWORKS_SIGNAL_QUALITY_MEASUREMENTS = name(Networks.class, "signal-quality-measurements");
    public static final String NETWORKS_DELTA_STATE_MEASUREMENTS = name(Networks.class, "delta-state-measurements");
    public static final String SIGNAL_INDEX_MEMORY_CLEANER_TIMER = name(Networks.class, "signal-index-table-cleaner");
    public static final String SIGNAL_INDEX_WRITER_TIMER = name(SignalIndexWriter.class, "write-timing");
    public static final String SIGNAL_INDEX_CLEANER_TIMER = name(SignalIndexCleaner.class, "clean-timing");
    public static final String SIGNAL_INDEX_READER_TIMER = name(SignalIndexManager.class, "read-timing");
    public static final String SIGNAL_INDEX_MONITOR_TIMER = name(SignalIndexAnomalyAlertMonitor.class, "monitor-timing");
    public static final String SIGNAL_INDEX_MONITOR_MEASUREMENTS = name(SignalIndexAnomalyAlertMonitor.class, "signal-monitor-measurements");
    public static final String BEACON_RATE_MONITOR_TIMER = name(BeaconRateAnomalyAlertMonitor.class, "monitor-timing");

}
