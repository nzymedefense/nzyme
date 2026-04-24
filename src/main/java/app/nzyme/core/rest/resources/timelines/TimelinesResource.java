package app.nzyme.core.rest.resources.timelines;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.responses.timelines.TimelineEventDetailsResponse;
import app.nzyme.core.rest.responses.timelines.TimelineResponse;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.Timelines;
import app.nzyme.core.timelines.db.TimelineEventEntry;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.List;
import java.util.UUID;


@Path("/api/timelines")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class TimelinesResource extends UserAuthenticatedResource {

    @Inject
    private NzymeNode nzyme;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JodaModule())
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private final static int GAP_THRESHOLD = 10;

    @GET
    @Path("/show/type/{addressType}/address/{address}")
    public Response findOne(@Context SecurityContext sc,
                            @PathParam("addressType") TimelineAddressType addressType,
                            @PathParam("address") String address,
                            @QueryParam("organization_id") @NotNull UUID organizationId,
                            @QueryParam("tenant_id") @NotNull UUID tenantId,
                            @QueryParam("time_range") String timeRangeParameter,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset) {
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Timelines timelines = new Timelines(nzyme);
        long total = timelines.countAllEventsOfAddress(organizationId, tenantId, addressType, address, timeRange, GAP_THRESHOLD);

        List<TimelineEventDetailsResponse> events = Lists.newArrayList();
        for (TimelineEventEntry e : timelines
                .findAllEventsOfAddress(organizationId, tenantId, addressType, address, timeRange, GAP_THRESHOLD, limit, offset)) {

            events.add(TimelineEventDetailsResponse.create(
                    e.uuid(),
                    e.address(),
                    e.addressType(),
                    e.eventType(),
                    objectMapper.readValue(e.eventDetails(), new TypeReference<>(){}),
                    e.timestamp()
            ));
        }

        return Response.ok(TimelineResponse.create(total, events)).build();
    }

}
