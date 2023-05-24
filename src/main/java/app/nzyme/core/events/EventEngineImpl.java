package app.nzyme.core.events;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.db.EventEntry;
import app.nzyme.core.events.types.EventType;
import app.nzyme.core.events.types.SystemEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EventEngineImpl implements EventEngine {

    private final NzymeNode nzyme;

    public EventEngineImpl(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    public void processEvent(SystemEvent event, @Nullable UUID eventOwnerOrganizationId, @Nullable UUID eventOwnerTenantId) {
        // Store in database.
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO events(organization_id, tenant_id, event_type, reference, " +
                                "details, created_at) VALUES(:organization_id, :tenant_id, :event_type, :reference, " +
                                ":details, NOW())")
                        .bind("organization_id", eventOwnerOrganizationId)
                        .bind("tenant_id", eventOwnerTenantId)
                        .bind("event_type", EventType.SYSTEM)
                        .bind("reference", event.type())
                        .bind("details", event.details())
                        .execute()
        );

        // Find all subscribers of event.

        // Process.
    }

    @Override
    public long countAllEventsOfAllOrganizations() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM events")
                        .mapTo(Long.class)
                        .one()
        );
    }

    @Override
    public long countAllEventsOfOrganization(UUID organizationId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM events WHERE organization_id = :organization_id")
                        .bind("organization_id", organizationId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    @Override
    public List<EventEntry> findAllEventsOfAllOrganizations(List<String> eventTypes, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM events WHERE event_type IN (<event_types>) " +
                                "ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
                        .bindList("event_types", eventTypes)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(EventEntry.class)
                        .list()
        );
    }

    @Override
    public List<EventEntry> findAllEventsOfOrganization(List<String> eventTypes, UUID organizationId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM events WHERE organization_id = :organization_id " +
                                "AND event_type IN (<event_types>) ORDER BY created_at DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bindList("event_types", eventTypes)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(EventEntry.class)
                        .list()
        );
    }

}
