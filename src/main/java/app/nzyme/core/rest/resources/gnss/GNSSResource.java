package app.nzyme.core.rest.resources.gnss;


import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.shared.db.GenericIntegerHistogramEntry;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.Filters;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Maps;
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
import java.util.UUID;

@Path("/api/gnss")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class GNSSResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/time/deviation/histogram")
    public Response timeDeviationHistogram(@Context SecurityContext sc,
                                           @QueryParam("time_range") @Valid String timeRangeParameter,
                                           @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, Integer> values = Maps.newHashMap();
        for (GenericIntegerHistogramEntry e : nzyme.getGnss()
                .getTimeDeviationHistogram(timeRange, bucketing, taps)) {
            values.put(e.bucket(), e.value());
        }

        return Response.ok(values).build();
    }

}
