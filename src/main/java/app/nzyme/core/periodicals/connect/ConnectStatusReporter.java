package app.nzyme.core.periodicals.connect;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.connect.*;
import app.nzyme.core.connect.reports.ConnectHealthIndicatorReport;
import app.nzyme.core.connect.reports.ConnectNodeLogCountReport;
import app.nzyme.core.connect.reports.ConnectStatusReport;
import app.nzyme.core.connect.reports.ConnectThroughputReport;
import app.nzyme.core.distributed.MetricExternalName;
import app.nzyme.core.distributed.NodeInformation;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.rest.resources.system.connect.api.ConnectApiStatusResponse;
import app.nzyme.core.taps.db.metrics.BucketSize;
import app.nzyme.core.taps.db.metrics.TapMetricsAggregation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ConnectStatusReporter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(ConnectStatusReporter.class);

    private final NzymeNode nzyme;
    private final OkHttpClient httpClient;
    private final ObjectMapper om;

    public ConnectStatusReporter(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        this.om = new ObjectMapper()
                .registerModule(new JodaModule())
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void execute() {
        if (!nzyme.getConnect().isEnabled()) {
            LOG.debug("Not running Connect status reporter. Connect is disabled.");
            return;
        }

        LOG.debug("Running Connect status reporter.");

        try {
            NodeInformation.Info ni = new NodeInformation().collect();

            ConnectStatusReport report = ConnectStatusReport.create(
                    nzyme.getVersion().getShortVersionString(),
                    nzyme.getNodeInformation().id().toString(),
                    nzyme.getNodeInformation().name(),
                    DateTime.now(),
                    getSystemProperty("java.vendor"),
                    getSystemProperty("java.version"),
                    getSystemProperty("os.name"),
                    getSystemProperty("os.arch"),
                    getSystemProperty("os.version"),
                    buildHealthIndicatorsReport(),
                    buildThroughputReport(),
                    buildLogCountReport(),
                    ni.cpuSystemLoad(),
                    (ni.memoryUsed()*100.0)/ni.memoryTotal(),
                    (ni.heapUsed()*100.0)/ni.heapTotal()
            );

            byte[] body = om.writeValueAsBytes(report);

            HttpUrl url = HttpUrl.get(nzyme.getConnect().getApiUri())
                    .newBuilder()
                    .addPathSegment("status")
                    .addPathSegment("report")
                    .build();

            Request request = new Request.Builder()
                    .post(RequestBody.create(body))
                    .url(url)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + nzyme.getConnect().getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .addHeader(HttpHeaders.USER_AGENT, "nzyme-node")
                    .build();


            try(Response response = httpClient.newCall(request).execute()) {
                if (response.code() != 201) {
                    LOG.error("Could not report node status to Connect at [{}]. Expected HTTP <201> but " +
                            "received HTTP <{}>.", nzyme.getConnect().getApiUri(), response.code());
                } else {
                    // Successful report submission.
                    LOG.debug("Successfully submitted Connect status report.");
                    nzyme.getDatabaseCoreRegistry().setValue(
                            ConnectRegistryKeys.LAST_SUCCESSFUL_REPORT_SUBMISSION.key(),
                            DateTime.now(DateTimeZone.UTC).toString()
                    );

                    // The response contains all enabled services. Store it.
                    if (response.body() != null) {
                        ObjectMapper om = new ObjectMapper();
                        om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
                        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        ConnectApiStatusResponse responseData = om.readValue(
                                response.body().bytes(), ConnectApiStatusResponse.class
                        );

                        nzyme.getDatabaseCoreRegistry().setValue(
                                ConnectRegistryKeys.PROVIDED_SERVICES.key(),
                                om.writeValueAsString(responseData.providedData())
                        );
                    } else {
                        LOG.error("Connect API status report response had an empty body.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Could not submit status report to Connect at [{}]..",
                    nzyme.getConnect().getApiUri(), e);
        }
    }

    private String getSystemProperty(String key) {
        try {
            String prop = System.getProperty(key);
            if (prop == null || prop.isBlank()) {
                return "UNKNOWN";
            } else {
                return prop;
            }
        } catch(Exception e) {
            return "UNKNOWN";
        }
    }

    private List<ConnectHealthIndicatorReport> buildHealthIndicatorsReport() {
        Optional<List<IndicatorStatus>> indicatorStatus = nzyme.getHealthMonitor().getIndicatorStatus();

        if (indicatorStatus.isEmpty()) {
            return Lists.newArrayList();
        }

        List<ConnectHealthIndicatorReport> report = Lists.newArrayList();
        for (IndicatorStatus status : indicatorStatus.get()) {
            report.add(ConnectHealthIndicatorReport.create(
                    status.indicatorName(),
                    status.indicatorId(),
                    status.lastChecked(),
                    status.resultLevel(),
                    status.active()
            ));
        }

        return report;
    }

    private List<ConnectThroughputReport> buildThroughputReport() {
        Optional<Map<DateTime, TapMetricsAggregation>> histo = nzyme.getTapManager().findMetricsGaugeHistogram(
                null,
                "system.captures.throughput_bit_sec",
                1,
                BucketSize.MINUTE
        );

        List<ConnectThroughputReport> report = Lists.newArrayList();
        if (histo.isEmpty()) {
            return report;
        } else {
            for (TapMetricsAggregation e : histo.get().values()) {
                report.add(ConnectThroughputReport.create(
                        e.bucket(),
                        e.average(),
                        e.maximum(),
                        e.minimum()
                ));
            }
        }

        return report;
    }

    private ConnectNodeLogCountReport buildLogCountReport() {
        UUID nodeId = nzyme.getNodeManager().getLocalNodeId();

        return nzyme.getDatabase().withHandle(handle -> {
            long trace = nzyme.getNodeManager().findLatestActiveMetricsGaugeValue(
                    nodeId, MetricExternalName.LOG_COUNTS_TRACE.database_label, handle
            ).orElse(0D).longValue();
            long debug = nzyme.getNodeManager().findLatestActiveMetricsGaugeValue(
                    nodeId, MetricExternalName.LOG_COUNTS_DEBUG.database_label, handle
            ).orElse(0D).longValue();
            long info = nzyme.getNodeManager().findLatestActiveMetricsGaugeValue(
                    nodeId, MetricExternalName.LOG_COUNTS_INFO.database_label, handle
            ).orElse(0D).longValue();
            long warn = nzyme.getNodeManager().findLatestActiveMetricsGaugeValue(
                    nodeId, MetricExternalName.LOG_COUNTS_WARN.database_label, handle
            ).orElse(0D).longValue();
            long error = nzyme.getNodeManager().findLatestActiveMetricsGaugeValue(
                    nodeId, MetricExternalName.LOG_COUNTS_ERROR.database_label, handle
            ).orElse(0D).longValue();
            long fatal = nzyme.getNodeManager().findLatestActiveMetricsGaugeValue(
                    nodeId, MetricExternalName.LOG_COUNTS_FATAL.database_label, handle
            ).orElse(0D).longValue();

            return ConnectNodeLogCountReport.create(trace, debug, info, warn, error, fatal);
        });
    }

    @Override
    public String getName() {
        return "ConnectStatusReporter";
    }

}
