package app.nzyme.core.rest.resources.locations;

/*
 * This is the generic locations resource used for reading high-level location information from across subsystems. It
 * is for all users and not to be confused wit the location management APIs in the OrganizationsResource, which is
 * limited to org admins.
 */

import app.nzyme.core.NzymeNode;
import app.nzyme.core.floorplans.db.TenantLocationEntry;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.responses.floorplans.TenantLocationDetailsResponse;
import app.nzyme.core.rest.responses.locations.LocationSummaryResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/api/locations/organizations/{organization_id}/tenants/{tenant_id}")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class LocationsResource extends UserAuthenticatedResource {


    @Inject
    private NzymeNode nzyme;

    @GET
    public Response findAll(@Context SecurityContext sc,
                            @PathParam("organization_id") UUID organizationId,
                            @PathParam("tenant_id") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<LocationSummaryResponse> locations = Lists.newArrayList();
        for (TenantLocationEntry location : nzyme.getAuthenticationService()
                .findAllTenantLocations(organizationId, tenantId, Integer.MAX_VALUE, 0)) {
            locations.add(LocationSummaryResponse.create(
                    location.uuid(),
                    location.name()
            ));
        }

        return Response.ok(locations).build();
    }


}
