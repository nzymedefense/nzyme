package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.ethernet.socks.db.SocksTunnelEntry;
import app.nzyme.core.ethernet.tcp.db.TcpSessionEntry;
import app.nzyme.core.rest.RestHelpers;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import app.nzyme.core.rest.responses.ethernet.socks.SocksTunnelDetailsResponse;
import app.nzyme.core.rest.responses.ethernet.socks.SocksTunnelsListResponse;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/ethernet/socks")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class SocksResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/tunnels")
    public Response tunnels(@Context SecurityContext sc,
                            @QueryParam("time_range") @Valid String timeRangeParameter,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset,
                            @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long total = nzyme.getEthernet().socks().countAllTunnels(timeRange, taps);

        List<SocksTunnelDetailsResponse> tunnels = Lists.newArrayList();
        for (SocksTunnelEntry t : nzyme.getEthernet().socks().findAllTunnels(timeRange, limit, offset, taps)) {
            // Get underlying TCP session. (Can be NULL)
            Optional<TcpSessionEntry> tcpSession = nzyme.getEthernet().tcp()
                    .findSessionBySessionKey(t.tcpSessionKey(), t.establishedAt(), taps);

            L4AddressResponse client = null;
            L4AddressResponse socksServer = null;
            if (tcpSession.isPresent()) {
                client = RestHelpers.L4AddressDataToResponse(L4Type.TCP, tcpSession.get().source());
                socksServer = RestHelpers.L4AddressDataToResponse(L4Type.TCP, tcpSession.get().destination());
            }

            tunnels.add(SocksTunnelDetailsResponse.create(
                    t.uuid(),
                    client,
                    socksServer,
                    t.tcpSessionKey(),
                    t.socksType(),
                    t.authenticationStatus(),
                    t.handshakeStatus(),
                    t.connectionStatus(),
                    t.username(),
                    t.tunneledBytes(),
                    t.tunneledDestinationAddress(),
                    t.tunneledDestinationHost(),
                    t.tunneledDestinationPort(),
                    t.establishedAt(),
                    t.terminatedAt(),
                    t.mostRecentSegmentTime(),
                    t.updatedAt(),
                    t.createdAt()
            ));
        }

        return Response.ok(SocksTunnelsListResponse.create(total, tunnels)).build();
    }

}
