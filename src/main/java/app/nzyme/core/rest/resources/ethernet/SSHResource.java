package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.ethernet.ssh.db.SSHSessionEntry;
import app.nzyme.core.ethernet.tcp.db.TcpSessionEntry;
import app.nzyme.core.rest.RestHelpers;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import app.nzyme.core.rest.responses.ethernet.ssh.SSHSessionDetailsResponse;
import app.nzyme.core.rest.responses.ethernet.ssh.SSHSessionsListResponse;
import app.nzyme.core.rest.responses.ethernet.ssh.SSHVersionResponse;
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

@Path("/api/ethernet/ssh")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class SSHResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/sessions")
    public Response sessions(@Context SecurityContext sc,
                             @QueryParam("time_range") @Valid String timeRangeParameter,
                             @QueryParam("limit") int limit,
                             @QueryParam("offset") int offset,
                             @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long total = nzyme.getEthernet().ssh().countAllSessions(timeRange, taps);

        List<SSHSessionDetailsResponse> sessions = Lists.newArrayList();
        for (SSHSessionEntry s : nzyme.getEthernet().ssh().findAllSessions(timeRange, limit, offset, taps)) {
            // Get underlying TCP session. (Can be NULL)
            Optional<TcpSessionEntry> tcpSession = nzyme.getEthernet().tcp()
                    .findSessionBySessionKey(s.tcpSessionKey(), s.establishedAt(), taps);

            L4AddressResponse client = null;
            L4AddressResponse server = null;
            if (tcpSession.isPresent()) {
                client = RestHelpers.L4AddressDataToResponse(L4Type.TCP, tcpSession.get().source());
                server = RestHelpers.L4AddressDataToResponse(L4Type.TCP, tcpSession.get().destination());
            }

            sessions.add(SSHSessionDetailsResponse.create(
                    s.uuid(),
                    client,
                    server,
                    SSHVersionResponse.create(
                            s.clientVersionVersion(),
                            s.clientVersionSoftware(),
                            s.clientVersionComments()
                    ),
                    SSHVersionResponse.create(
                            s.serverVersionVersion(),
                            s.serverVersionSoftware(),
                            s.serverVersionComments()
                    ),
                    tcpSession.map(tcpSessionEntry -> RestHelpers.tcpSessionStateToGeneric(tcpSessionEntry.state()))
                            .orElse("Invalid"),
                    s.tunneledBytes(),
                    s.establishedAt(),
                    s.terminatedAt(),
                    s.mostRecentSegmentTime(),
                    s.updatedAt(),
                    s.createdAt()
            ));
        }

        return Response.ok(SSHSessionsListResponse.create(total, sessions)).build();
    }

}