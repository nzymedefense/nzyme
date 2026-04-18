package app.nzyme.core.timelines;

import app.nzyme.core.NzymeNode;
import org.joda.time.DateTime;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.Map;
import java.util.UUID;

public class Timelines {

    private final NzymeNode nzyme;
    private final ObjectMapper objectMapper;

    public Timelines(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.objectMapper = JsonMapper.builder()
                .addModule(new JodaModule())
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    public void writeDot11TimelineEvent(UUID organizationId,
                                        UUID tenantId,
                                        TimelineAddressType addressType,
                                        String address,
                                        TimelineEventType eventType,
                                        Map<String, Object> eventDetails,
                                        DateTime timestamp) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_timeline_events(uuid, organization_id, tenant_id, " +
                                "address, address_type, event_type, event_details, timestamp) " +
                                "VALUES(:uuid, :organization_id, :tenant_id, :address, :address_type, " +
                                ":event_type, :event_details::jsonb, :timestamp)")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("address", address)
                        .bind("address_type", addressType)
                        .bind("event_type", eventType)
                        .bind("event_details", objectMapper.writeValueAsString(eventDetails))
                        .bind("timestamp", timestamp)
                        .execute()
        );
    }

}
