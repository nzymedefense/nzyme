package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.generic.NumberBucketAggregationResult;
import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.ethernet.dns.DNSTransaction;
import app.nzyme.core.ethernet.dns.db.*;
import app.nzyme.core.rest.RestHelpers;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.ethernet.L4AddressAttributesResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressGeoResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressTypeResponse;
import app.nzyme.core.rest.responses.ethernet.dns.*;
import app.nzyme.core.rest.responses.shared.*;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.plugin.rest.security.RESTSecured;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.*;

@Path("/api/ethernet/dns")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class DNSResource extends TapDataHandlingResource {

    private static final Logger LOG = LogManager.getLogger(TapDataHandlingResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/global/charts/{type}")
    public Response globalCharts(@Context SecurityContext sc,
                                 @QueryParam("time_range") @Valid String timeRangeParameter,
                                 @QueryParam("taps") String tapIds,
                                 @PathParam("type") String type) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        List<DNSStatisticsBucket> statistics = nzyme.getEthernet().dns().getStatistics(timeRange, bucketing, taps);

        Map<DateTime, Long> response = Maps.newHashMap();
        for (DNSStatisticsBucket b : statistics) {
            Long value;
            switch (type) {
                case "request_count":
                    value = b.requestCount();
                    break;
                case "request_bytes":
                    value = b.requestBytes();
                    break;
                case "response_count":
                    value = b.responseCount();
                    break;
                case "response_bytes":
                    value = b.responseBytes();
                    break;
                case "nxdomain_count":
                    value = b.nxdomainCount();
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST).build();
            }

            response.put(b.bucket(), value);
        }

        return Response.ok(response).build();
    }

    @GET
    @Path("/global/statistics/{type}")
    public Response globalStatistics(@Context SecurityContext sc,
                                     @QueryParam("time_range") @Valid String timeRangeParameter,
                                     @QueryParam("taps") String tapIds,
                                     @PathParam("type") String type) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        DNSTrafficSummary trafficSummary = nzyme.getEthernet().dns().getTrafficSummary(timeRange, taps);

        long value;
        switch (type) {
            case "packets":
                value = trafficSummary.totalPackets();
                break;
            case "traffic":
                value = trafficSummary.totalTrafficBytes();
                break;
            case "nxdomains":
                value = trafficSummary.totalNxdomains();
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Map<String, Long> response = Maps.newHashMap();
        response.put("value", value);

        return Response.ok(response).build();
    }

    @GET
    @Path("/global/pairs")
    public Response globalPairs(@Context SecurityContext sc,
                                @QueryParam("time_range") @Valid String timeRangeParameter,
                                @QueryParam("limit") int limit,
                                @QueryParam("offset") int offset,
                                @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long total = nzyme.getEthernet().dns().countPairs(timeRange, taps);

        List<ThreeColumnTableHistogramValueResponse> values = Lists.newArrayList();
        for (DNSPairSummary ps : nzyme.getEthernet().dns().getPairs(timeRange, limit, offset, taps)) {
            Map<String, Object> requestCount = Maps.newHashMap();
            requestCount.put("title", ps.requestCount());

            values.add(ThreeColumnTableHistogramValueResponse.create(
                    HistogramValueStructureResponse.create(
                            RestHelpers.L4AddressDataToResponse(L4Type.UDP, ps.server()),
                            HistogramValueType.L4_ADDRESS,
                            null
                    ),
                    HistogramValueStructureResponse.create(
                            ps.clientCount(),
                            HistogramValueType.INTEGER,
                            null
                    ),
                    HistogramValueStructureResponse.create(
                            requestCount,
                            HistogramValueType.DNS_TRANSACTION_LOG_LINK,
                            null
                    ),
                    ps.server().address() + ":" + ps.server().port()
            ));
        }

        return Response.ok(ThreeColumnTableHistogramResponse.create(total, values)).build();
    }

    @GET
    @Path("/global/entropylog")
    public Response globalEntropyLog(@Context SecurityContext sc,
                                     @QueryParam("time_range") @Valid String timeRangeParameter,
                                     @QueryParam("limit") int limit,
                                     @QueryParam("offset") int offset,
                                     @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long total = nzyme.getEthernet().dns().countAllEntropyLogs(timeRange, taps);
        List<DNSEntropyLogResponse> logs = Lists.newArrayList();

        // Pull all required information and build response.
        List<DNSEntropyLogEntry> entropyLogs = nzyme.getEthernet().dns()
                .findAllEntropyLogs(timeRange, limit, offset, taps);

        nzyme.getDatabase().useHandle(handle -> {
            for (DNSEntropyLogEntry el : entropyLogs) {
                Optional<DNSTransaction> transaction = nzyme.getEthernet().dns()
                        .findTransaction(el.transactionId(), el.timestamp(), taps, handle);

                if (transaction.isEmpty()) {
                    continue;
                }

                DNSLogDataResponse query = logToResponse(transaction.get().query());
                List<DNSLogDataResponse> responses = Lists.newArrayList();
                for (DNSLogEntry response : transaction.get().responses()) {
                    responses.add(logToResponse(response));
                }

                logs.add(DNSEntropyLogResponse.create(query, responses, el.entropy(), el.entropyMean(), el.zscore()));
            }
        });

        return Response.ok(DNSEntropyLogListResponse.create(total, logs)).build();
    }

    @GET
    @Path("/transactions/log")
    public Response transactionLog(@Context SecurityContext sc,
                                   @QueryParam("time_range") @Valid String timeRangeParameter,
                                   @QueryParam("limit") int limit,
                                   @QueryParam("offset") int offset,
                                   @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long total = nzyme.getEthernet().dns().countAllTransactions(timeRange, taps);

        List<DNSLogEntryResponse> transactions = Lists.newArrayList();
        for (DNSTransaction transaction : nzyme.getEthernet().dns()
                .findAllTransactions(timeRange, limit, offset, taps)) {
            DNSLogDataResponse query = logToResponse(transaction.query());
            List<DNSLogDataResponse> responses = Lists.newArrayList();
            for (DNSLogEntry response : transaction.responses()) {
                responses.add(logToResponse(response));
            }

            transactions.add(DNSLogEntryResponse.create(query, responses));
        }

        transactions.sort((o1, o2) -> o2.query().timestamp().compareTo(o1.query().timestamp()));

        return Response.ok(DNSLogListResponse.create(total, transactions)).build();
    }

    @GET
    @Path("/transactions/charts/count")
    public Response transactionChart(@Context SecurityContext sc,
                                     @QueryParam("time_range") @Valid String timeRangeParameter,
                                     @QueryParam("limit") int limit,
                                     @QueryParam("offset") int offset,
                                     @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, Long> response = Maps.newHashMap();
        for (NumberBucketAggregationResult r : nzyme.getEthernet().dns()
                .getTransactionCountHistogram("query", timeRange, bucketing, taps)) {

            response.put(r.bucket(), r.count());
        }

        return Response.ok(response).build();
    }

    private DNSLogDataResponse logToResponse(DNSLogEntry log) {
        return DNSLogDataResponse.create(
                log.uuid(),
                log.tapUUID(),
                log.transactionId(),
                RestHelpers.L4AddressDataToResponse(L4Type.UDP, log.client()),
                RestHelpers.L4AddressDataToResponse(L4Type.UDP, log.server()),
                log.dataValue(),
                log.dataValueEtld(),
                log.dataType(),
                log.dnsType(),
                log.timestamp(),
                log.createdAt()
        );
    }

}
