package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.ethernet.dhcp.db.DHCPTransactionEntry;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressContextResponse;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import app.nzyme.core.rest.responses.ethernet.dhcp.DHCPTransactionDetailsResponse;
import app.nzyme.core.rest.responses.ethernet.dhcp.DHCPTransactionsListResponse;
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
import org.joda.time.Duration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/ethernet/dhcp")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class DHCPResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/transactions")
    public Response transactions(@Context SecurityContext sc,
                                 @QueryParam("time_range") @Valid String timeRangeParameter,
                                 @QueryParam("limit") int limit,
                                 @QueryParam("offset") int offset,
                                 @QueryParam("taps") String tapIds) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long total = nzyme.getEthernet().dhcp().countAllTransactions(timeRange, taps);
        List<DHCPTransactionDetailsResponse> txs = Lists.newArrayList();
        for (DHCPTransactionEntry tx : nzyme.getEthernet().dhcp().findAllTransactions(timeRange, limit, offset, taps)) {
            Long duration = null;

            if (tx.isComplete()) {
                duration = new Duration(tx.firstPacket(), tx.latestPacket()).getMillis();
            }

            Optional<MacAddressContextEntry> clientMacContext = nzyme.getContextService().findMacAddressContext(
                    tx.clientMac(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            Optional<MacAddressContextEntry> serverMacContext;
            EthernetMacAddressResponse serverMacResponse;
            if (tx.serverMac() != null) {
                serverMacContext = nzyme.getContextService().findMacAddressContext(
                        tx.serverMac(),
                        authenticatedUser.getOrganizationId(),
                        authenticatedUser.getTenantId()
                );
                serverMacResponse = EthernetMacAddressResponse.create(
                        tx.serverMac(),
                        nzyme.getOuiService().lookup(tx.serverMac()).orElse(null),
                        serverMacContext.map(macAddressContextEntry ->
                                        EthernetMacAddressContextResponse.create(
                                                macAddressContextEntry.name(),
                                                macAddressContextEntry.description()
                                        ))
                                .orElse(null)
                );
            } else {
                serverMacResponse = null;
            }

            txs.add(DHCPTransactionDetailsResponse.create(
                    tx.transactionId(),
                    tx.transactionType(),
                    EthernetMacAddressResponse.create(
                            tx.clientMac(),
                            nzyme.getOuiService().lookup(tx.clientMac()).orElse(null),
                            clientMacContext.map(macAddressContextEntry ->
                                            EthernetMacAddressContextResponse.create(
                                                    macAddressContextEntry.name(),
                                                    macAddressContextEntry.description()
                                            ))
                                    .orElse(null)
                    ),
                    tx.additionalClientMacs(),
                    serverMacResponse,
                    tx.additionalServerMacs(),
                    tx.offeredIpAddresses(),
                    tx.requestedIpAddress(),
                    tx.optionsFingerprint(),
                    tx.additionalOptionsFingerprints(),
                    tx.timestamps(),
                    tx.firstPacket(),
                    tx.latestPacket(),
                    tx.notes(),
                    tx.isComplete(),
                    duration
            ));
        }

        return Response.ok(DHCPTransactionsListResponse.create(total, txs)).build();
    }

}
