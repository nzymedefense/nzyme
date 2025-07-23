package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.assets.db.AssetEntry;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.ethernet.arp.ARP;
import app.nzyme.core.ethernet.arp.ARPExplainer;
import app.nzyme.core.ethernet.arp.db.ARPStatisticsBucket;
import app.nzyme.core.ethernet.arp.db.ArpPacketEntry;
import app.nzyme.core.ethernet.arp.db.ArpSenderTargetCountPair;
import app.nzyme.core.rest.RestHelpers;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressContextResponse;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import app.nzyme.core.rest.responses.dot11.Dot11MacLinkMetadataResponse;
import app.nzyme.core.rest.responses.ethernet.*;
import app.nzyme.core.rest.responses.ethernet.arp.ArpPacketDetailsResponse;
import app.nzyme.core.rest.responses.ethernet.arp.ArpPacketsListResponse;
import app.nzyme.core.rest.responses.ethernet.arp.ArpStatisticsBucketResponse;
import app.nzyme.core.rest.responses.shared.*;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.Filters;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Nullable;
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
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/api/ethernet/arp")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class ArpResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/packets")
    public Response packets(@Context SecurityContext sc,
                            @QueryParam("organization_id") UUID organizationId,
                            @QueryParam("tenant_id") UUID tenantId,
                            @QueryParam("time_range") @Valid String timeRangeParameter,
                            @QueryParam("filters") String filtersParameter,
                            @QueryParam("order_column") @Nullable String orderColumnParam,
                            @QueryParam("order_direction") @Nullable String orderDirectionParam,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset,
                            @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Filters filters = parseFiltersQueryParameter(filtersParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ARP.OrderColumn orderColumn = ARP.OrderColumn.TIMESTAMP;
        OrderDirection orderDirection = OrderDirection.DESC;
        if (orderColumnParam != null && orderDirectionParam != null) {
            try {
                orderColumn = ARP.OrderColumn.valueOf(orderColumnParam.toUpperCase());
                orderDirection = OrderDirection.valueOf(orderDirectionParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        long total = nzyme.getEthernet().arp().countAllPackets(timeRange, filters, taps);

        List<ArpPacketDetailsResponse> packets = Lists.newArrayList();
        for (ArpPacketEntry packet : nzyme.getEthernet().arp()
                .findAllPackets(timeRange, filters, limit, offset, orderColumn, orderDirection, taps)) {

           packets.add(buildDetailsResponse(packet, organizationId, tenantId));
        }

        return Response.ok(ArpPacketsListResponse.create(total, packets)).build();
    }

    @GET
    @Path("/statistics")
    public Response statistics(@Context SecurityContext sc,
                               @QueryParam("organization_id") UUID organizationId,
                               @QueryParam("tenant_id") UUID tenantId,
                               @QueryParam("time_range") @Valid String timeRangeParameter,
                               @QueryParam("filters") String filtersParameter,
                               @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Filters filters = parseFiltersQueryParameter(filtersParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<DateTime, ArpStatisticsBucketResponse> statistics = Maps.newHashMap();
        for (ARPStatisticsBucket s : nzyme.getEthernet().arp().getStatistics(timeRange, bucketing, filters, taps)) {
            statistics.put(s.bucket(), ArpStatisticsBucketResponse.create(
                    s.totalCount(),
                    s.requestCount(),
                    s.replyCount(),
                    s.requestToReplyRatio(),
                    s.gratuitousRequestCount(),
                    s.gratuitousReplyCount()
            ));
        }

        return Response.ok(statistics).build();
    }

    @GET
    @Path("/histograms/requesters/pairs")
    public Response requesterPairs(@Context SecurityContext sc,
                                   @QueryParam("organization_id") UUID organizationId,
                                   @QueryParam("tenant_id") UUID tenantId,
                                   @QueryParam("time_range") @Valid String timeRangeParameter,
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

        long topRequesterPairsCount = nzyme.getEthernet().arp()
                .countPairs("Request", timeRange,  filters, taps);

        List<ThreeColumnTableHistogramValueResponse> topRequesterPairs = buildPairs(
                "Request", organizationId, tenantId, timeRange, filters, limit, offset, taps
        );

        return Response.ok(ThreeColumnTableHistogramResponse.create(topRequesterPairsCount, topRequesterPairs)).build();
    }

    @GET
    @Path("/histograms/responders/pairs")
    public Response responderPairs(@Context SecurityContext sc,
                                   @QueryParam("organization_id") UUID organizationId,
                                   @QueryParam("tenant_id") UUID tenantId,
                                   @QueryParam("time_range") @Valid String timeRangeParameter,
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

        long topRequesterPairsCount = nzyme.getEthernet().arp()
                .countPairs("Reply", timeRange,  filters, taps);

        List<ThreeColumnTableHistogramValueResponse> topRequesterPairs = buildPairs(
                "Reply", organizationId, tenantId, timeRange, filters, limit, offset, taps
        );

        return Response.ok(ThreeColumnTableHistogramResponse.create(topRequesterPairsCount, topRequesterPairs)).build();
    }

    private ArpPacketDetailsResponse buildDetailsResponse(ArpPacketEntry packet, UUID organizationId, UUID tenantId) {
        Optional<AssetEntry> ethernetSourceAsset = nzyme.getAssetsManager()
                .findAssetByMac(packet.ethernetSourceMac(), organizationId, tenantId);
        Optional<MacAddressContextEntry> ethernetSourceMacContext = nzyme.getContextService()
                .findMacAddressContext(packet.ethernetSourceMac(), organizationId, tenantId);
        Optional<AssetEntry> ethernetDestinationAsset = nzyme.getAssetsManager()
                .findAssetByMac(packet.ethernetDestinationMac(), organizationId, tenantId);
        Optional<MacAddressContextEntry> ethernetDestinationMacContext = nzyme.getContextService()
                .findMacAddressContext(packet.ethernetDestinationMac(), organizationId, tenantId);

        return ArpPacketDetailsResponse.create(
                packet.tapUUID(),
                EthernetMacAddressResponse.create(
                        packet.ethernetSourceMac(),
                        nzyme.getOuiService().lookup(packet.ethernetSourceMac()).orElse(null),
                        ethernetSourceAsset.map(AssetEntry::uuid).orElse(null),
                        ethernetSourceMacContext.map(ctx ->
                                EthernetMacAddressContextResponse.create(
                                        ctx.name(),
                                        ctx.description()
                                )
                        ).orElse(null)
                ),
                EthernetMacAddressResponse.create(
                        packet.ethernetDestinationMac(),
                        nzyme.getOuiService().lookup(packet.ethernetDestinationMac()).orElse(null),
                        ethernetDestinationAsset.map(AssetEntry::uuid).orElse(null),
                        ethernetDestinationMacContext.map(ctx ->
                                EthernetMacAddressContextResponse.create(
                                        ctx.name(),
                                        ctx.description()
                                )
                        ).orElse(null)
                ),
                packet.hardwareType(),
                packet.protocolType(),
                packet.operation(),
                RestHelpers.internalAddressDataToResponse(
                        nzyme, packet.arpSenderMac(), packet.arpSenderAddress(), organizationId, tenantId
                ),
                RestHelpers.internalAddressDataToResponse(
                        nzyme, packet.arpTargetMac(), packet.arpTargetAddress(), organizationId, tenantId
                ),
                packet.size(),
                packet.timestamp(),
                ARPExplainer.explain(
                        packet.ethernetDestinationMac(),
                        packet.operation(),
                        packet.arpSenderMac(),
                        packet.arpSenderAddress(),
                        packet.arpTargetMac(),
                        packet.arpTargetAddress()
                )
        );
    }

    private List<ThreeColumnTableHistogramValueResponse> buildPairs(String operation,
                                                                    UUID organizationId,
                                                                    UUID tenantId,
                                                                    TimeRange timeRange,
                                                                    Filters filters,
                                                                    int limit,
                                                                    int offset,
                                                                    List<UUID> taps) {
        List<ThreeColumnTableHistogramValueResponse> pairs = Lists.newArrayList();
        for (ArpSenderTargetCountPair pair : nzyme.getEthernet().arp()
                .getPairs(operation, timeRange, filters, limit, offset, taps)) {
            Optional<MacAddressContextEntry> senderMacContext = nzyme.getContextService().findMacAddressContext(
                    pair.senderMac(),
                    organizationId,
                    tenantId
            );
            Optional<MacAddressContextEntry> targetMacContext = nzyme.getContextService().findMacAddressContext(
                    pair.targetMac(),
                    organizationId,
                    tenantId
            );

            Optional<AssetEntry> senderAsset = nzyme.getAssetsManager()
                    .findAssetByMac(pair.senderMac(), organizationId, tenantId);
            Optional<AssetEntry> targetAsset = nzyme.getAssetsManager()
                    .findAssetByMac(pair.targetMac(), organizationId, tenantId);

            pairs.add(ThreeColumnTableHistogramValueResponse.create(
                    HistogramValueStructureResponse.create(
                            pair.senderMac(),
                            HistogramValueType.ETHERNET_MAC,
                            EthernetMacAddressResponse.create(
                                    pair.senderMac(),
                                    nzyme.getOuiService().lookup(pair.senderMac()).orElse(null),
                                    senderAsset.map(AssetEntry::uuid).orElse(null),
                                    senderMacContext.map(ctx ->
                                            EthernetMacAddressContextResponse.create(
                                                    ctx.name(),
                                                    ctx.description()
                                            )
                                    ).orElse(null)
                            )
                    ),
                    HistogramValueStructureResponse.create(
                            pair.targetMac(),
                            HistogramValueType.ETHERNET_MAC,
                            EthernetMacAddressResponse.create(
                                    pair.targetMac(),
                                    nzyme.getOuiService().lookup(pair.targetMac()).orElse(null),
                                    targetAsset.map(AssetEntry::uuid).orElse(null),
                                    targetMacContext.map(ctx ->
                                            EthernetMacAddressContextResponse.create(
                                                    ctx.name(),
                                                    ctx.description()
                                            )
                                    ).orElse(null)
                            )
                    ),
                    HistogramValueStructureResponse.create(pair.count(), HistogramValueType.INTEGER, null),
                    pair.senderMac() + " -> " + pair.targetMac()
            ));
        }

        return pairs;
    }

}
