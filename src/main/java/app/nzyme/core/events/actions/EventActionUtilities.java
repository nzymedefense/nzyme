package app.nzyme.core.events.actions;

import app.nzyme.core.events.db.EventActionEntry;
import app.nzyme.core.events.db.EventEntry;
import app.nzyme.core.events.types.EventActionType;
import app.nzyme.core.events.types.EventType;
import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.rest.responses.events.EventActionDetailsResponse;
import app.nzyme.core.rest.responses.events.EventTypeDetailsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EventActionUtilities {

    private static final ObjectMapper om = new ObjectMapper();

    public static EventActionDetailsResponse eventActionEntryToResponse(EventActionEntry ea, List<SystemEventType> subscribedToEvents) {
        Map<String, Object> configuration;


        try {
            configuration = om.readValue(ea.configuration(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not read event action configuration.", e);
        }

        List<EventTypeDetailsResponse> subscriptions = Lists.newArrayList();
        for (SystemEventType subscription : subscribedToEvents) {
            subscriptions.add(EventTypeDetailsResponse.create(
                    subscription.name(),
                    subscription.getCategory().name(),
                    subscription.getCategory().getHumanReadableName(),
                    subscription.getHumanReadableName(),
                    subscription.getDescription(),
                    Collections.emptyList()
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
                subscriptions,
                ea.createdAt(),
                ea.updatedAt()
        );
    }

}
