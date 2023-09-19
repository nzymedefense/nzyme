package app.nzyme.core.events.actions;

import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.events.db.EventActionEntry;
import app.nzyme.core.events.types.EventActionType;
import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.rest.responses.events.DetectionEventTypeDetailsResponse;
import app.nzyme.core.rest.responses.events.EventActionDetailsResponse;
import app.nzyme.core.rest.responses.events.SystemEventTypeDetailsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EventActionUtilities {

    private static final ObjectMapper om = new ObjectMapper();

    public static EventActionDetailsResponse eventActionEntryToResponse(EventActionEntry ea,
                                                                        List<SystemEventType> subscribedSystemEvents,
                                                                        List<DetectionType> subscribedDetectionEvents) {
        Map<String, Object> configuration;


        try {
            configuration = om.readValue(ea.configuration(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not read event action configuration.", e);
        }

        List<SystemEventTypeDetailsResponse> systemEventSubscriptions = Lists.newArrayList();
        for (SystemEventType subscription : subscribedSystemEvents) {
            systemEventSubscriptions.add(SystemEventTypeDetailsResponse.create(
                    subscription.name(),
                    subscription.getCategory().name(),
                    subscription.getCategory().getHumanReadableName(),
                    subscription.getHumanReadableName(),
                    subscription.getDescription(),
                    Collections.emptyList()
            ));
        }

        List<DetectionEventTypeDetailsResponse> detectionEventSubscriptions = Lists.newArrayList();
        for (DetectionType subscription : subscribedDetectionEvents) {
            detectionEventSubscriptions.add(DetectionEventTypeDetailsResponse.create(
                    subscription.name(),
                    subscription.getTitle(),
                    subscription.getSubsystem().name()
            ));
        }

        EventActionType actionType = EventActionType.valueOf(ea.actionType());
        return EventActionDetailsResponse.create(
                ea.uuid(),
                ea.organizationId(),
                actionType.name(),
                actionType.getHumanReadable(),
                ea.name(),
                ea.description(),
                configuration,
                systemEventSubscriptions,
                detectionEventSubscriptions,
                ea.createdAt(),
                ea.updatedAt()
        );
    }

}
