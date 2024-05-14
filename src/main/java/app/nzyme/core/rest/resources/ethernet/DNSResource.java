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
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import org.joda.time.DateTime;

import jakarta.inject.Inject;
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

        Long value;
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
