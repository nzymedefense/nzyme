package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.shared.*;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.ethernet.dns.db.DNSPairSummary;
import app.nzyme.core.ethernet.dns.db.DNSStatisticsBucket;
import app.nzyme.core.ethernet.dns.db.DNSTrafficSummary;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.responses.ethernet.dns.DNSStatisticsBucketResponse;
import app.nzyme.core.rest.responses.ethernet.dns.DNSStatisticsResponse;
import app.nzyme.core.rest.responses.ethernet.dns.DNSTrafficSummaryResponse;
import jakarta.validation.Valid;
import org.joda.time.DateTime;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/ethernet/dns")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class DNSResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/statistics")
    public Response statistics(@Context SecurityContext sc,
                               @QueryParam("time_range") @Valid String timeRangeParameter,
                               @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        List<DNSStatisticsBucket> statistics = nzyme.getEthernet().dns().getStatistics(timeRange, bucketing, taps);

        Map<DateTime, DNSStatisticsBucketResponse> buckets = Maps.newHashMap();
        for (DNSStatisticsBucket b : statistics) {
            buckets.put(b.bucket(), DNSStatisticsBucketResponse.create(
                    b.bucket(),
                    b.requestCount(),
                    b.requestBytes(),
                    b.responseCount(),
                    b.responseBytes(),
                    b.nxdomainCount()
            ));
        }

        DNSTrafficSummary trafficSummary = nzyme.getEthernet().dns().getTrafficSummary(timeRange, taps);


        return Response.ok(
                DNSStatisticsResponse.create(
                        buckets,
                        DNSTrafficSummaryResponse.create(
                                trafficSummary.totalPackets(),
                                trafficSummary.totalTrafficBytes(),
                                trafficSummary.totalNxdomains()
                        )
                )
        ).build();
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
            values.add(ThreeColumnTableHistogramValueResponse.create(
                    HistogramValueStructureResponse.create(
                            ps.server(),
                            HistogramValueType.IP_ADDRESS,
                            null
                    ),
                    HistogramValueStructureResponse.create(
                            ps.clientCount(),
                            HistogramValueType.INTEGER,
                            null
                    ),
                    HistogramValueStructureResponse.create(
                            ps.requestCount(),
                            HistogramValueType.INTEGER,
                            null
                    ),
                    ps.server()
            ));
        }

        return Response.ok(ThreeColumnTableHistogramResponse.create(total, values)).build();
    }

}
