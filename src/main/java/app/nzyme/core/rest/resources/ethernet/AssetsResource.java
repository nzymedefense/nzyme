package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.assets.AssetManager;
import app.nzyme.core.assets.db.AssetEntry;
import app.nzyme.core.assets.db.AssetHostnameEntry;
import app.nzyme.core.assets.db.AssetIpAddressEntry;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.context.db.MacAddressTransparentContextEntry;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressContextResponse;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import app.nzyme.core.rest.responses.ethernet.assets.*;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.*;

@Path("/api/ethernet/assets")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class AssetsResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response allAssets(@Context SecurityContext sc,
                              @QueryParam("organization_id") UUID organizationId,
                              @QueryParam("tenant_id") UUID tenantId,
                              @QueryParam("time_range") @Valid String timeRangeParameter,
                              @QueryParam("order_column") @Nullable String orderColumnParam,
                              @QueryParam("order_direction") @Nullable String orderDirectionParam,
                              @QueryParam("limit") int limit,
                              @QueryParam("offset") int offset) {
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        AssetManager.OrderColumn orderColumn = AssetManager.OrderColumn.LAST_SEEN;
        OrderDirection orderDirection = OrderDirection.DESC;
        if (orderColumnParam != null && orderDirectionParam != null) {
            try {
                orderColumn = AssetManager.OrderColumn.valueOf(orderColumnParam.toUpperCase());
                orderDirection = OrderDirection.valueOf(orderDirectionParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        long total = nzyme.getAssetsManager().countAssets(timeRange, organizationId, tenantId);

        List<AssetSummaryResponse> assets = Lists.newArrayList();
        for (AssetEntry asset : nzyme.getAssetsManager()
                .findAllAssets(organizationId, tenantId, timeRange, limit, offset, orderColumn, orderDirection)) {

            Optional<MacAddressContextEntry> context = nzyme.getContextService().findMacAddressContext(
                    asset.mac(), organizationId, tenantId
            );

            Set<String> hostnames = Sets.newHashSet();
            Set<String> ipAddresses = Sets.newHashSet();
            if (context.isPresent()) {
                for (MacAddressTransparentContextEntry tpx : nzyme.getContextService()
                        .findTransparentMacAddressContext(context.get().id())) {
                    switch (tpx.type()) {
                        case "HOSTNAME":
                            hostnames.add(tpx.hostname());
                            break;
                        case "IP_ADDRESS":
                            if (tpx.ipAddress() != null) {
                                ipAddresses.add(tpx.ipAddress().getHostAddress());
                            }
                            break;
                    }
                }
            }

            assets.add(AssetSummaryResponse.create(
                    asset.uuid(),
                    EthernetMacAddressResponse.create(
                            asset.mac(),
                            nzyme.getOuiService().lookup(asset.mac()).orElse(null),
                            context.map(ctx ->
                                    EthernetMacAddressContextResponse.create(
                                            ctx.name(),
                                            ctx.description()
                                    )
                            ).orElse(null)
                    ),
                    nzyme.getOuiService().lookup(asset.mac()).orElse(null),
                    context.map(MacAddressContextEntry::name).orElse(null),
                    hostnames,
                    ipAddresses,
                    asset.dhcpFingerprintInitial(),
                    asset.dhcpFingerprintRenew(),
                    asset.dhcpFingerprintReboot(),
                    asset.dhcpFingerprintRebind(),
                    asset.firstSeen(),
                    asset.lastSeen()
            ));
        }

        return Response.ok(AssetSummariesListResponse.create(total, assets)).build();
    }

    @GET
    @Path("/show/{asset_id}")
    public Response one(@Context SecurityContext sc,
                        @PathParam("asset_id") UUID assetId,
                        @QueryParam("organization_id") UUID organizationId,
                        @QueryParam("tenant_id") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<AssetEntry> asset = nzyme.getAssetsManager().findAsset(assetId, organizationId, tenantId);

        if (asset.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<MacAddressContextEntry> context = nzyme.getContextService().findMacAddressContext(
                asset.get().mac(), organizationId, tenantId
        );

        return Response.ok(AssetDetailsResponse.create(
                asset.get().uuid(),
                EthernetMacAddressResponse.create(
                        asset.get().mac(),
                        nzyme.getOuiService().lookup(asset.get().mac()).orElse(null),
                        context.map(ctx ->
                                EthernetMacAddressContextResponse.create(
                                        ctx.name(),
                                        ctx.description()
                                )
                        ).orElse(null)
                ),
                nzyme.getOuiService().lookup(asset.get().mac()).orElse(null),
                context.map(MacAddressContextEntry::name).orElse(null),
                asset.get().dhcpFingerprintInitial(),
                asset.get().dhcpFingerprintRenew(),
                asset.get().dhcpFingerprintReboot(),
                asset.get().dhcpFingerprintRebind(),
                asset.get().seenDhcp(),
                asset.get().seenTcp(),
                asset.get().firstSeen(),
                asset.get().lastSeen()
        )).build();
    }

    @GET
    @Path("/show/{asset_id}/hostnames")
    public Response hostnames(@Context SecurityContext sc,
                              @PathParam("asset_id") UUID assetId,
                              @QueryParam("organization_id") UUID organizationId,
                              @QueryParam("tenant_id") UUID tenantId,
                              @QueryParam("time_range") @Valid String timeRangeParameter,
                              @QueryParam("order_column") @Nullable String orderColumnParam,
                              @QueryParam("order_direction") @Nullable String orderDirectionParam,
                              @QueryParam("limit") int limit,
                              @QueryParam("offset") int offset) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        AssetManager.HostnameOrderColumn orderColumn = AssetManager.HostnameOrderColumn.LAST_SEEN;
        OrderDirection orderDirection = OrderDirection.DESC;
        if (orderColumnParam != null && orderDirectionParam != null) {
            try {
                orderColumn = AssetManager.HostnameOrderColumn.valueOf(orderColumnParam.toUpperCase());
                orderDirection = OrderDirection.valueOf(orderDirectionParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        Optional<AssetEntry> asset = nzyme.getAssetsManager().findAsset(assetId, organizationId, tenantId);

        if (asset.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long total = nzyme.getAssetsManager().countHostnamesOfAsset(asset.get().id(), timeRange);

        List<AssetHostnameDetailsResponse> hostnames = Lists.newArrayList();
        for (AssetHostnameEntry h : nzyme.getAssetsManager()
                .findHostnamesOfAsset(asset.get().id(), timeRange, limit, offset, orderColumn, orderDirection)) {
            hostnames.add(AssetHostnameDetailsResponse.create(
                    h.uuid(),
                    h.hostname(),
                    h.source(),
                    h.firstSeen(),
                    h.lastSeen()
            ));
        }

        return Response.ok(AssetHostnamesListResponse.create(total, hostnames)).build();
    }


    @GET
    @Path("/show/{asset_id}/ip_addresses")
    public Response ipAddresses(@Context SecurityContext sc,
                                @PathParam("asset_id") UUID assetId,
                                @QueryParam("organization_id") UUID organizationId,
                                @QueryParam("tenant_id") UUID tenantId,
                                @QueryParam("time_range") @Valid String timeRangeParameter,
                                @QueryParam("order_column") @Nullable String orderColumnParam,
                                @QueryParam("order_direction") @Nullable String orderDirectionParam,
                                @QueryParam("limit") int limit,
                                @QueryParam("offset") int offset) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        AssetManager.IpAddressOrderColumn orderColumn = AssetManager.IpAddressOrderColumn.LAST_SEEN;
        OrderDirection orderDirection = OrderDirection.DESC;
        if (orderColumnParam != null && orderDirectionParam != null) {
            try {
                orderColumn = AssetManager.IpAddressOrderColumn.valueOf(orderColumnParam.toUpperCase());
                orderDirection = OrderDirection.valueOf(orderDirectionParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        Optional<AssetEntry> asset = nzyme.getAssetsManager().findAsset(assetId, organizationId, tenantId);

        if (asset.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long total = nzyme.getAssetsManager().countIpAddressesOfAsset(asset.get().id(), timeRange);

        List<AssetIpAddressDetailsResponse> addresses = Lists.newArrayList();
        for (AssetIpAddressEntry i : nzyme.getAssetsManager()
                .findIpAddressesOfAsset(asset.get().id(), timeRange, limit, offset, orderColumn, orderDirection)) {
            addresses.add(AssetIpAddressDetailsResponse.create(
                    i.uuid(),
                    i.address(),
                    i.source(),
                    i.firstSeen(),
                    i.lastSeen()
            ));
        }

        return Response.ok(AssetIpAddressesListResponse.create(total, addresses)).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "ethernet_assets_manage" })
    @Path("/show/{asset_id}/hostnames/{hostname_id}/organization/{organization_id}/tenant/{tenant_id}")
    public Response deleteHostname(@Context SecurityContext sc,
                                   @PathParam("asset_id") UUID assetId,
                                   @PathParam("hostname_id") UUID hostnameId,
                                   @PathParam("organization_id") UUID organizationId,
                                   @PathParam("tenant_id") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<AssetEntry> asset = nzyme.getAssetsManager().findAsset(assetId, organizationId, tenantId);

        if (asset.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAssetsManager().deleteHostnameOfAsset(asset.get().id(), hostnameId);

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "ethernet_assets_manage" })
    @Path("/show/{asset_id}/ip_addresses/{address_id}/organization/{organization_id}/tenant/{tenant_id}")
    public Response deleteIpAddress(@Context SecurityContext sc,
                                    @PathParam("asset_id") UUID assetId,
                                    @PathParam("address_id") UUID addressId,
                                    @PathParam("organization_id") UUID organizationId,
                                    @PathParam("tenant_id") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<AssetEntry> asset = nzyme.getAssetsManager().findAsset(assetId, organizationId, tenantId);

        if (asset.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAssetsManager().deleteIpAddressOfAsset(asset.get().id(), addressId);

        return Response.ok().build();
    }

}