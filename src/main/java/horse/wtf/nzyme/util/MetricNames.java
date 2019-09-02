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
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.periodicals.alerting.beaconrate.BeaconRateAnomalyAlertMonitor;

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
    public static final String BEACON_RATE_MONITOR_TIMER = name(BeaconRateAnomalyAlertMonitor.class, "monitor-timing");

}
