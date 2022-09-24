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

package horse.wtf.nzyme.rest.resources.system;

import com.codahale.metrics.*;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.NzymeLeader;
import app.nzyme.plugin.RESTSecured;
import horse.wtf.nzyme.rest.responses.metrics.*;
import horse.wtf.nzyme.util.MetricNames;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/api/system/metrics")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    @Inject
    private NzymeLeader nzyme;

    @GET
    public Response all() {
        Map<String, Object> metrics = Maps.newHashMap();

        metrics.put(
                "total_frames",
                MeterResponse.fromMeter(getMeter(MetricNames.FRAME_COUNT))
        );

        metrics.put(
                "frame_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.FRAME_TIMER))
        );

        metrics.put(
                "oui_lookup_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.OUI_LOOKUP_TIMING))
        );

        metrics.put(
                "tagged_params_parse_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.TAGGED_PARAMS_PARSE_TIMING))
        );

        metrics.put(
                "tagged_params_fingerprint_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.TAGGED_PARAMS_FINGERPRINT_TIMING))
        );

        metrics.put(
                "mem_heap_init",
                GaugeResponse.fromGauge(nzyme.getMetrics().getGauges().get(MetricNames.MEMORY_HEAP_INIT))
        );

        metrics.put(
                "mem_heap_max",
                GaugeResponse.fromGauge(nzyme.getMetrics().getGauges().get(MetricNames.MEMORY_HEAP_MAX))
        );

        metrics.put(
                "mem_heap_used",
                GaugeResponse.fromGauge(nzyme.getMetrics().getGauges().get(MetricNames.MEMORY_HEAP_USED))
        );

        metrics.put(
                "mem_heap_usage_percent",
                GaugeResponse.fromGauge(nzyme.getMetrics().getGauges().get(MetricNames.MEMORY_HEAP_USAGE_PERCENT))
        );

        metrics.put(
                "mem_nonheap_init",
                GaugeResponse.fromGauge(nzyme.getMetrics().getGauges().get(MetricNames.MEMORY_NONHEAP_INIT))
        );

        metrics.put(
                "mem_nonheap_max",
                GaugeResponse.fromGauge(nzyme.getMetrics().getGauges().get(MetricNames.MEMORY_NONHEAP_MAX))
        );

        metrics.put(
                "mem_nonheap_used",
                GaugeResponse.fromGauge(nzyme.getMetrics().getGauges().get(MetricNames.MEMORY_NONHEAP_USED))
        );

        metrics.put(
                "mem_nonheap_usage_percent",
                GaugeResponse.fromGauge(nzyme.getMetrics().getGauges().get(MetricNames.MEMORY_NONHEAP_USAGE_PERCENT))
        );

        metrics.put(
                "beaconrate_monitor_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.BEACON_RATE_MONITOR_TIMING))
        );

        metrics.put(
                "signaltables_mutex_wait",
                TimerResponse.fromTimer(getTimer(MetricNames.SIGNAL_TABLES_MUTEX_WAIT))
        );

        metrics.put(
                "signaltrack_monitor_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.SIGNAL_TRACK_MONITOR_TIMING))
        );

        metrics.put(
                "contact_identifier_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.CONTACT_IDENTIFIER_TIMING))
        );

        metrics.put(
                "groundstation_rx",
                CounterResponse.fromCounter(getCounter(MetricNames.GROUNDSTATION_RX))
        );

        metrics.put(
                "groundstation_tx",
                CounterResponse.fromCounter(getCounter(MetricNames.GROUNDSTATION_TX))
        );

        metrics.put(
                "groundstation_encryption_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.GROUNDSTATION_ENCRYPTION_TIMING))
        );

        metrics.put(
                "groundstation_queue_size",
                GaugeResponse.fromGauge(getGauge(MetricNames.GROUNDSTATION_QUEUE_SIZE))
        );

        metrics.put(
                "remote_frames_received",
                CounterResponse.fromCounter(getCounter(MetricNames.REMOTE_FRAMES_RECEIVED))
        );

        metrics.put(
                "remote_frames_timing",
                TimerResponse.fromTimer(getTimer(MetricNames.REMOTE_FRAMES_TIMING))
        );

        metrics.put(
                "database_size",
                GaugeResponse.fromGauge(getGauge(MetricNames.DATABASE_SIZE))
        );

        metrics.put(
                "tap_table_report_sizes",
                HistogramResponse.fromSnapshot(getHistogram(MetricNames.TAP_TABLE_REQUEST_SIZES).getSnapshot())
        );

        return Response.ok(MetricsListResponse.create(metrics.size(), metrics)).build();
    }

    private Meter getMeter(String name) {
        Meter meter = nzyme.getMetrics().getMeters().get(name);
        return meter == null ? new Meter() : meter;
    }

    private Timer getTimer(String name) {
        Timer timer = nzyme.getMetrics().getTimers().get(name);
        return timer == null ? new Timer() : timer;
    }

    private Counter getCounter(String name) {
        Counter counter = nzyme.getMetrics().getCounters().get(name);
        return counter == null ? new Counter() : counter;
    }

    private Gauge getGauge(String name) {
        Gauge gauge = nzyme.getMetrics().getGauges().get(name);
        return gauge == null ? (Gauge<String>) () -> "" : gauge;
    }

    private Histogram getHistogram(String name) {
        Histogram histogram = nzyme.getMetrics().getHistograms().get(name);
        return histogram == null ? new Histogram(new UniformReservoir()) : histogram;
    }

}