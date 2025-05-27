package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.ethernet.dhcp.DHCP;
import app.nzyme.core.ethernet.dhcp.db.DHCPTransactionEntry;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressContextResponse;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import app.nzyme.core.rest.responses.ethernet.dhcp.DHCPTimelineStepResponse;
import app.nzyme.core.rest.responses.ethernet.dhcp.DHCPTransactionDetailsResponse;
import app.nzyme.core.rest.responses.ethernet.dhcp.DHCPTransactionsListResponse;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.*;

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
                                 @QueryParam("order_column") @Nullable String orderColumnParam,
                                 @QueryParam("order_direction") @Nullable String orderDirectionParam,
                                 @QueryParam("limit") int limit,
                                 @QueryParam("offset") int offset,
                                 @QueryParam("taps") String tapIds) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        DHCP.OrderColumn orderColumn = DHCP.OrderColumn.INITIATED_AT;
        OrderDirection orderDirection = OrderDirection.DESC;
        if (orderColumnParam != null && orderDirectionParam != null) {
            try {
                orderColumn = DHCP.OrderColumn.valueOf(orderColumnParam.toUpperCase());
                orderDirection = OrderDirection.valueOf(orderDirectionParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        long total = nzyme.getEthernet().dhcp().countAllTransactions(timeRange, taps);
        List<DHCPTransactionDetailsResponse> txs = Lists.newArrayList();
        for (DHCPTransactionEntry tx : nzyme.getEthernet().dhcp()
                .findAllTransactions(timeRange, limit, offset, orderColumn, orderDirection, taps)) {
            txs.add(buildTransactionResponse(tx, authenticatedUser));
        }

        return Response.ok(DHCPTransactionsListResponse.create(total, txs)).build();
    }

    @GET
    @Path("/transactions/show/{transaction_id}")
    public Response transaction(@Context SecurityContext sc,
                                @PathParam("transaction_id") long transactionId,
                                @QueryParam("transaction_time") String transactionTimeP,
                                @QueryParam("taps") String tapIds) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        DateTime transactionTime;

        try {
            transactionTime = DateTime.parse(transactionTimeP);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<DHCPTransactionEntry> txe = nzyme.getEthernet().dhcp()
                .findTransaction(transactionId, transactionTime, taps);

        if (txe.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(buildTransactionResponse(txe.get(), authenticatedUser)).build();
    }

    private DHCPTransactionDetailsResponse buildTransactionResponse(DHCPTransactionEntry tx,
                                                                    AuthenticatedUser authenticatedUser) {
        Long duration = null;
        if (tx.isComplete()) {
            duration = new Duration(tx.firstPacket(), tx.latestPacket()).getMillis();
        }

        Optional<MacAddressContextEntry> clientMacContext = nzyme.getContextService().findMacAddressContext(
                tx.clientMac(),
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId()
        );

        // We change the structure of the timestamps to be easier to use in the client.
        List<DHCPTimelineStepResponse> unsortedTimeline = Lists.newArrayList();
        for (Map.Entry<String, List<String>> step : tx.timestamps().entrySet()) {
            for (String timestamp : step.getValue()) {
                unsortedTimeline.add(DHCPTimelineStepResponse.create(step.getKey(), timestamp));
            }
        }

        List<DHCPTimelineStepResponse> sortedTimeline = tx.timestamps().entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(ts -> DHCPTimelineStepResponse.create(e.getKey(), ts)))
                .sorted(Comparator.comparing(DHCPTimelineStepResponse::timestamp))
                .toList();

        return DHCPTransactionDetailsResponse.create(
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
                buildServerMacResponse(tx, authenticatedUser),
                tx.additionalServerMacs(),
                tx.offeredIpAddresses(),
                tx.requestedIpAddress(),
                tx.options(),
                tx.additionalOptions(),
                tx.fingerprint(),
                tx.additionalFingerprints(),
                tx.vendorClass(),
                tx.additionalVendorClasses(),
                sortedTimeline,
                tx.firstPacket(),
                tx.latestPacket(),
                tx.notes(),
                tx.isSuccessful(),
                tx.isComplete(),
                duration
        );
    }

    @Nullable
    private EthernetMacAddressResponse buildServerMacResponse(DHCPTransactionEntry tx,
                                                              AuthenticatedUser authenticatedUser) {
        if (tx.serverMac() != null) {
            Optional<MacAddressContextEntry> serverMacContext = nzyme.getContextService().findMacAddressContext(
                    tx.serverMac(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            return EthernetMacAddressResponse.create(
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
            return null;
        }
    }



}
