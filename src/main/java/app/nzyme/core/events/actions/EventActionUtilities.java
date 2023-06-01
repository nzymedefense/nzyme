package app.nzyme.core.events.actions;

import app.nzyme.core.events.db.EventActionEntry;
import app.nzyme.core.events.types.EventActionType;
import app.nzyme.core.rest.responses.events.EventActionDetailsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class EventActionUtilities {

    private static final ObjectMapper om = new ObjectMapper();

    public static EventActionDetailsResponse eventActionEntryToResponse(EventActionEntry ea) {
        Map<String, Object> configuration;


        try {
            configuration = om.readValue(ea.configuration(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not read event action configuration.", e);
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
                ea.createdAt(),
                ea.updatedAt()
        );
    }

}
