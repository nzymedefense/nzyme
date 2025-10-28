package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.assets.db.AssetEntry;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.database.NumberNumberAggregationResult;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.database.generic.NumberNumberNumberAggregationResult;
import app.nzyme.core.database.generic.StringNumberAggregationResult;
import app.nzyme.core.database.generic.StringNumberNumberAggregationResult;
import app.nzyme.core.ethernet.l4.L4;
import app.nzyme.core.ethernet.l4.db.L4Numbers;
import app.nzyme.core.ethernet.l4.db.L4Session;
import app.nzyme.core.ethernet.l4.db.L4StatisticsBucket;
import app.nzyme.core.rest.RestHelpers;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.ethernet.*;
import app.nzyme.core.rest.responses.ethernet.l4.L4NumbersResponse;
import app.nzyme.core.rest.responses.ethernet.l4.L4SessionDetailsResponse;
import app.nzyme.core.rest.responses.ethernet.l4.L4SessionsListResponse;
import app.nzyme.core.rest.responses.ethernet.l4.L4StatisticsBucketResponse;
import app.nzyme.core.rest.responses.shared.*;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.Filters;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/api/ethernet/l4")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class L4Resource extends TapDataHandlingResource  {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/sessions")
    public Response allSessions(@Context SecurityContext sc,
                                @QueryParam("organization_id") UUID organizationId,
                                @QueryParam("tenant_id") UUID tenantId,
                                @QueryParam("time_range") String timeRangeParameter,
                                @QueryParam("filters") String filtersParameter,
                                @QueryParam("limit") int limit,
                                @QueryParam("offset") int offset,
                                @QueryParam("order_column") @Nullable String orderColumnParam,
                                @QueryParam("order_direction") @Nullable String orderDirectionParam,
                                @QueryParam("taps") String tapIds) {

        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Filters filters = parseFiltersQueryParameter(filtersParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        L4.OrderColumn orderColumn = L4.OrderColumn.MOST_RECENT_SEGMENT_TIME;
        OrderDirection orderDirection = OrderDirection.DESC;
        if (orderColumnParam != null && orderDirectionParam != null) {
            try {
                orderColumn = L4.OrderColumn.valueOf(orderColumnParam.toUpperCase());
                orderDirection = OrderDirection.valueOf(orderDirectionParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        long total = nzyme.getEthernet().l4().countAllSessions(timeRange, filters, taps);

        List<L4SessionDetailsResponse> sessions = Lists.newArrayList();
        for (L4Session session : nzyme.getEthernet().l4()
                .findAllSessions(timeRange, filters, limit, offset, orderColumn, orderDirection, taps)) {
            sessions.add(L4SessionDetailsResponse.create(
                    session.sessionKey(),
                    L4AddressTypeResponse.valueOf(session.l4Type().toString()),
                    RestHelpers.L4AddressDataToResponse(
                            nzyme, organizationId, tenantId, session.l4Type(), session.source()
                    ),
                    RestHelpers.L4AddressDataToResponse(
                            nzyme, organizationId, tenantId, session.l4Type(), session.destination()
                    ),
                    session.bytesRxCount()+session.bytesTxCount(),
                    session.bytesRxCount(),
                    session.bytesTxCount(),
                    session.segmentsCount(),
                    session.startTime(),
                    session.endTime(),
                    session.mostRecentSegmentTime(),
                    session.state()
            ));
        }

        return Response.ok(L4SessionsListResponse.create(total, sessions)).build();
    }

    @GET
    @Path("/sessions/statistics")
    public Response sessionsStatistics(@Context SecurityContext sc,
                                       @QueryParam("time_range") @Valid String timeRangeParameter,
                                       @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, L4StatisticsBucketResponse> statistics = Maps.newHashMap();
        for (L4StatisticsBucket b : nzyme.getEthernet().l4().getStatistics(timeRange, bucketing, taps)) {
            statistics.put(b.bucket(), L4StatisticsBucketResponse.create(
                    b.bytesRxTcp(),
                    b.bytesTxTcp(),
                    b.bytesRxInternalTcp(),
                    b.bytesTxInternalTcp(),
                    b.bytesRxUdp(),
                    b.bytesTxUdp(),
                    b.bytesRxInternalUdp(),
                    b.bytesTxInternalUdp(),
                    b.segmentsTcp(),
                    b.datagramsUdp(),
                    b.sessionsTcp(),
                    b.sessionsUdp(),
                    b.sessionsInternalTcp(),
                    b.sessionsInternalUdp()
            ));
        }

        L4Numbers totals = nzyme.getEthernet().l4().getTotals(timeRange, taps);
        L4NumbersResponse numbers = L4NumbersResponse.create(
                totals.bytesTcp(),
                totals.bytesInternalTcp(),
                totals.bytesUdp(),
                totals.bytesInternalUdp(),
                totals.segmentsTcp(),
                totals.datagramsUdp()
        );

        Map<String, Object> response = Maps.newHashMap();
        response.put("statistics", statistics);
        response.put("numbers", numbers);

        return Response.ok(response).build();
    }

    @GET
    @Path("/sessions/histograms/sources/traffic/top")
    public Response topTrafficSources(@Context SecurityContext sc,
                                      @QueryParam("organization_id") UUID organizationId,
                                      @QueryParam("tenant_id") UUID tenantId,
                                      @QueryParam("time_range") String timeRangeParameter,
                                      @QueryParam("filters") String filtersParameter,
                                      @QueryParam("limit") int limit,
                                      @QueryParam("offset") int offset,
                                      @QueryParam("taps") String tapIds) {

        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Filters filters = parseFiltersQueryParameter(filtersParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long total = nzyme.getEthernet().l4().countTopTrafficSources(timeRange, filters, taps);

        List<ThreeColumnTableHistogramValueResponse> values = Lists.newArrayList();
        for (StringNumberNumberAggregationResult port : nzyme.getEthernet().l4()
                .getTopTrafficSources(timeRange, filters, limit, offset, taps)) {
            Optional<MacAddressContextEntry> sourceContext = nzyme.getContextService().findMacAddressContext(
                    port.key(),
                    organizationId,
                    tenantId
            );

            Optional<AssetEntry> sourceAsset = nzyme.getAssetsManager()
                    .findAssetByMac(port.key(), organizationId, tenantId);

            values.add(ThreeColumnTableHistogramValueResponse.create(
                    HistogramValueStructureResponse.create(port.key(),
                            HistogramValueType.ETHERNET_MAC,
                            EthernetMacAddressResponse.create(
                                    port.key(),
                                    nzyme.getOuiService().lookup(port.key()).orElse(null),
                                    sourceAsset.map(AssetEntry::uuid).orElse(null),
                                    sourceAsset.map(AssetEntry::isActive).orElse(null),
                                    sourceContext.map(ctx ->
                                            EthernetMacAddressContextResponse.create(
                                                    ctx.name(),
                                                    ctx.description()
                                            )
                                    ).orElse(null)
                            )
                    ),
                    HistogramValueStructureResponse.create(port.value1(), HistogramValueType.BYTES, null),
                    HistogramValueStructureResponse.create(port.value2(), HistogramValueType.BYTES, null),
                    port.key()
            ));
        }

        return Response.ok(ThreeColumnTableHistogramResponse.create(total, false, values)).build();
    }

    @GET
    @Path("/sessions/histograms/ports/destination/non-ephemeral/bottom")
    public Response leastCommonNonEphemeralDestinationPorts(@Context SecurityContext sc,
                                                            @QueryParam("time_range") String timeRangeParameter,
                                                            @QueryParam("filters") String filtersParameter,
                                                            @QueryParam("limit") int limit,
                                                            @QueryParam("offset") int offset,
                                                            @QueryParam("taps") String tapIds) {

        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Filters filters = parseFiltersQueryParameter(filtersParameter);

        long total = nzyme.getEthernet().l4().countDestinationPorts(timeRange, filters, taps);

        List<ThreeColumnTableHistogramValueResponse> values = Lists.newArrayList();
        for (NumberNumberNumberAggregationResult port : nzyme.getEthernet().l4()
                .getLeastCommonNonEphemeralDestinationPorts(timeRange, filters, limit, offset, taps)) {
            values.add(ThreeColumnTableHistogramValueResponse.create(
                    HistogramValueStructureResponse.create(port.key(), HistogramValueType.L4_PORT, null),
                    HistogramValueStructureResponse.create(port.value1(), HistogramValueType.INTEGER, null),
                    HistogramValueStructureResponse.create(port.value2(), HistogramValueType.BYTES, null),
                    String.valueOf(port.key())
            ));
        }

        return Response.ok(ThreeColumnTableHistogramResponse.create(total, false, values)).build();
    }

    @GET
    @Path("/ips/show/{ip_address}")
    public Response ipAddress(@Context SecurityContext sc,
                              @PathParam("ip_address") String ipAddress,
                              @QueryParam("organization_id") UUID organizationId,
                              @QueryParam("tenant_id") UUID tenantId,
                              @QueryParam("time_range") String timeRangeParameter,
                              @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!InetAddresses.isInetAddress(ipAddress)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // just return L4AddressResponse? check how we query data and if that gives all we need.

        return Response.ok().build();
    }

}
