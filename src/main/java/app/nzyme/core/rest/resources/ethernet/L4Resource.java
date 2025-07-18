package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.ethernet.l4.L4;
import app.nzyme.core.ethernet.l4.db.L4Session;
import app.nzyme.core.rest.RestHelpers;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.ethernet.*;
import app.nzyme.core.rest.responses.ethernet.l4.L4SessionDetailsResponse;
import app.nzyme.core.rest.responses.ethernet.l4.L4SessionsListResponse;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.Filters;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
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
                    session.bytesCount(),
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
