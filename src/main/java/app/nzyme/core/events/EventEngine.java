package app.nzyme.core.events;

import app.nzyme.core.events.db.EventEntry;
import app.nzyme.core.events.types.SystemEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface EventEngine {

    void processEvent(SystemEvent event, @Nullable UUID organizationId, @Nullable UUID tenantId);
    //void processEvent(DetectionEvent event, @Nullable UUID organizationId, @Nullable UUID tenantId);

    long countAllEventsOfAllOrganizations();
    long countAllEventsOfOrganization(UUID organizationId);
    List<EventEntry> findAllEventsOfAllOrganizations(List<String> eventTypes, int limit, int offset);
    List<EventEntry> findAllEventsOfOrganization(List<String> eventTypes, UUID organizationId, int limit, int offset);

}
