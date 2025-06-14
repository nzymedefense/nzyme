package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.assets.AssetManager;
import app.nzyme.core.assets.db.AssetEntry;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.context.db.MacAddressTransparentContextEntry;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressContextResponse;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import app.nzyme.core.rest.responses.ethernet.assets.AssetDetailsResponse;
import app.nzyme.core.rest.responses.ethernet.assets.AssetHostnameResponse;
import app.nzyme.core.rest.responses.ethernet.assets.AssetIpAddressResponse;
import app.nzyme.core.rest.responses.ethernet.assets.AssetsListResponse;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
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

        List<AssetDetailsResponse> assets = Lists.newArrayList();
        for (AssetEntry asset : nzyme.getAssetsManager()
                .findAllAssets(organizationId, tenantId, timeRange, limit, offset, orderColumn, orderDirection)) {
            assets.add(buildAssetDetailsResponse(asset, authenticatedUser));
        }

        return Response.ok(AssetsListResponse.create(total, assets)).build();
    }

    private AssetDetailsResponse buildAssetDetailsResponse(AssetEntry asset, AuthenticatedUser authenticatedUser) {
        Optional<MacAddressContextEntry> context = nzyme.getContextService().findMacAddressContext(
                asset.mac(),
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId()
        );

        List<AssetHostnameResponse> hostnames = Lists.newArrayList();
        List<AssetIpAddressResponse> ipAddresses = Lists.newArrayList();
        if (context.isPresent()) {
            for (MacAddressTransparentContextEntry tpx : nzyme.getContextService()
                    .findTransparentMacAddressContext(context.get().id())) {
                switch (tpx.type()) {
                    case "HOSTNAME":
                        hostnames.add(AssetHostnameResponse.create(
                                tpx.hostname(),
                                tpx.source(),
                                tpx.lastSeen()
                        ));
                        break;
                    case "IP_ADDRESS":
                        ipAddresses.add(AssetIpAddressResponse.create(
                                tpx.ipAddress().getHostAddress(),
                                tpx.source(),
                                tpx.lastSeen()
                        ));
                        break;
                }
            }
        }

        return AssetDetailsResponse.create(
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
                asset.dhcpFingerprintInitial(),
                asset.dhcpFingerprintRenew(),
                asset.dhcpFingerprintReboot(),
                asset.dhcpFingerprintRebind(),
                hostnames,
                ipAddresses,
                asset.firstSeen(),
                asset.lastSeen()
        );
    }

}