package app.nzyme.core.rest.resources.timelines;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.responses.llm.LLMResponse;
import app.nzyme.core.rest.responses.timelines.TimelineEventDetailsResponse;
import app.nzyme.core.rest.responses.timelines.TimelineResponse;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.llm.TimelineEventTextConverter;
import app.nzyme.core.timelines.Timelines;
import app.nzyme.core.timelines.db.TimelineEventEntry;
import app.nzyme.core.timelines.llm.TimelineLLMSummaryOutputConverter;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static app.nzyme.core.timelines.llm.TimelineEventTextConverter.computeLifecycleSummary;


@Path("/api/timelines")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class TimelinesResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(TimelinesResource.class);

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
                .findAllEventsOfAddress(organizationId, tenantId, addressType, address, timeRange, OrderDirection.DESC, GAP_THRESHOLD, limit, offset)) {

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

    @GET
    @Path("/show/type/{addressType}/address/{address}/summarize")
    public Response summarize(@Context SecurityContext sc,
                              @PathParam("addressType") TimelineAddressType addressType,
                              @PathParam("address") String address,
                              @QueryParam("organization_id") @NotNull UUID organizationId,
                              @QueryParam("tenant_id") @NotNull UUID tenantId,
                              @QueryParam("time_range") String timeRangeParameter) {
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Timelines timelines = new Timelines(nzyme);
        List<TimelineEventEntry> timeline = timelines.findAllEventsOfAddress(
                organizationId,
                tenantId,
                addressType,
                address,
                timeRange,
                OrderDirection.ASC,
                GAP_THRESHOLD,
                Integer.MAX_VALUE,
                0
        );

        // Requesting a summary of a timeline is a bad request and the UI should not let you do that.
        if (timeline.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String sensorEnvironment = "- Second floor office in a busy downtown area with a single sensor/tap.\n" +
                "- Our production SSIDs are \"Chipotle\", \"Chipotle_PSK\" and \"Chipotle_Guest\". These are provided as\n" +
                "  reference only — do not include them in any output field unless they appear in the\n" +
                "  timeline data below.\n" +
                "- Any other SSIDs observed are from neighboring businesses, passing devices, or unknown\n" +
                "  sources — not our infrastructure. Use this context when inferring device type and\n" +
                "  assessing threat level.";

        String scheduleSummary = TimelineEventTextConverter.computeScheduleSummary(timeline, timeRange.to());

        Map<String, String> params = Map.of(
                "START_TIME", timeRange.from().toString(),
                "END_TIME", timeRange.to().toString(),
                "TAP_COUNT",String.valueOf(nzyme.getTapManager().findAllTapsOfTenant(organizationId, tenantId).size()),
                "TIMELINE_EVENTS", TimelineEventTextConverter.eventToText(timeline),
                "SENSOR_ENVIRONMENT", sensorEnvironment,
                "ACTIVE_PERIODS", scheduleSummary,
                "LIFECYCLE_SUMMARY", computeLifecycleSummary(timeline, timeRange.from(), timeRange.to()),
                "FIRST_SEEN", timeline.getFirst().timestamp().toString("yyyy-MM-dd'T'HH:mmZ")
        );

        ChatResponse response = nzyme.getLLM().query(
                nzyme.getLLM().getPrompt("dot11_bssid_timeline_summary.md", params).get(),
                nzyme.getLLM().getSystemPrompt()
        );

        ObjectMapper om = new ObjectMapper();

        return Response.ok(LLMResponse.create(
                response.metadata().tokenUsage().inputTokenCount(),
                response.metadata().tokenUsage().outputTokenCount(),
                om.convertValue(TimelineLLMSummaryOutputConverter.parse(response.aiMessage().text()), new TypeReference<>() {})
        )).build();
    }

}
