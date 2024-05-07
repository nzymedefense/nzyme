package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.shared.GeoInformationResponse;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.ethernet.dns.db.DNSPairSummary;
import app.nzyme.core.ethernet.dns.db.DNSStatisticsBucket;
import app.nzyme.core.ethernet.dns.db.DNSTrafficSummary;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.responses.ethernet.dns.DNSPairSummaryResponse;
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

        List<DNSPairSummaryResponse> pairSummary = Lists.newArrayList();
        for (DNSPairSummary ps : nzyme.getEthernet().dns().getPairSummary(timeRange, 10, taps)) {
            pairSummary.add(DNSPairSummaryResponse.create(
                    ps.server(),
                    GeoInformationResponse.create(
                            ps.serverGeoAsnNumber(),
                            ps.serverGeoAsnName(),
                            ps.serverGeoAsnDomain(),
                            null,
                            ps.serverGeoCountryCode(),
                            null,
                            null
                    ),
                    ps.requestCount(),
                    ps.clientCount())
            );
        }

        return Response.ok(
                DNSStatisticsResponse.create(
                        buckets,
                        DNSTrafficSummaryResponse.create(
                                trafficSummary.totalPackets(),
                                trafficSummary.totalTrafficBytes(),
                                trafficSummary.totalNxdomains()
                        ),
                        pairSummary
                )
        ).build();
    }

}
