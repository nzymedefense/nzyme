package app.nzyme.core.rest.resources.locations;

/*
 * This is the generic locations resource used for reading high-level location information from across subsystems. It
 * is for all users and not to be confused wit the location management APIs in the OrganizationsResource, which is
 * limited to org admins.
 */

import app.nzyme.core.NzymeNode;
import app.nzyme.core.environment.EnvironmentData;
import app.nzyme.core.environment.LocationEnvironmentAlertDetails;
import app.nzyme.core.floorplans.db.TenantLocationEntry;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.responses.locations.LocationEnvironmentAlertDetailsResponse;
import app.nzyme.core.rest.responses.locations.LocationEnvironmentDataResponse;
import app.nzyme.core.rest.responses.locations.LocationSummaryResponse;
import app.nzyme.core.taps.Tap;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.nzyme.core.environment.EnvironmentService.alertIsCurrentlyRelevant;
import static app.nzyme.core.environment.EnvironmentService.severityOrdinal;

@Path("/api/locations/organizations/{organization_id}/tenants/{tenant_id}")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class LocationsResource extends UserAuthenticatedResource {

    private static final int MIN_ALERT_SEVERITY = 3;

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


            locations.add(buildLocationSummary(location, organizationId, tenantId));
        }

        return Response.ok(locations).build();
    }

    @GET
    @Path("/show/{location_id}")
    public Response findOne(@Context SecurityContext sc,
                            @PathParam("location_id") UUID locationId,
                            @PathParam("organization_id") UUID organizationId,
                            @PathParam("tenant_id") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<TenantLocationEntry> location = nzyme.getAuthenticationService()
                .findTenantLocation(locationId, organizationId, tenantId);

        if (location.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(buildLocationSummary(location.get(), organizationId, tenantId)).build();
    }

    private LocationSummaryResponse buildLocationSummary(TenantLocationEntry location,
                                                         UUID organizationId,
                                                         UUID tenantId) {
        List<Tap> taps = nzyme.getTapManager().findAllTapsAtLocation(location.uuid());

        int alerts = 0;
        for (Tap tap : taps) {
            alerts += (int) nzyme.getDetectionAlertService()
                    .countActiveAlertsOfTap(organizationId, tenantId, tap.uuid());
        }

        Optional<EnvironmentData> ed = nzyme.getEnvironmentService().getEnvironmentData(location.uuid());
        LocationEnvironmentDataResponse environmentDataResponse;
        if (ed.isPresent()) {
            EnvironmentData x = ed.get();
            List<LocationEnvironmentAlertDetailsResponse> eAlerts = Lists.newArrayList();
            for (LocationEnvironmentAlertDetails a : x.alerts()) {
                if (a.severity() != null && severityOrdinal(a.severity()) < MIN_ALERT_SEVERITY) {
                    continue;
                }

                if (!alertIsCurrentlyRelevant(a)) {
                    continue;
                }

                eAlerts.add(LocationEnvironmentAlertDetailsResponse.create(
                        a.event(),
                        a.severity(),
                        a.certainty(),
                        a.urgency(),
                        a.headline(),
                        a.description(),
                        a.senderName(),
                        a.effective(),
                        a.expires(),
                        a.ends()
                ));
            }

            environmentDataResponse = LocationEnvironmentDataResponse.create(
                    x.stationId(),
                    x.temperature(),
                    x.windDirection(),
                    x.windSpeed(),
                    x.windGust(),
                    x.visibility(),
                    eAlerts
            );
        } else {
            environmentDataResponse = null;
        }

        return LocationSummaryResponse.create(
                location.uuid(),
                location.name(),
                taps.size(),
                alerts,
                environmentDataResponse
        );
    }

}
