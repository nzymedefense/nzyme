package app.nzyme.core.context;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.Subsystem;
import app.nzyme.core.context.db.MacAddressContextEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ContextService {

    private final NzymeNode nzyme;

    public ContextService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void createMacAddressContext(String macAddress,
                                        Subsystem subsystem,
                                        String name,
                                        @Nullable String description,
                                        @Nullable String notes,
                                        UUID organizationId,
                                        UUID tenantId) {
        nzyme.getDatabase().useHandle(handle ->
            handle.createUpdate("INSERT INTO context_mac_addresses(mac_address, uuid, subsystem, name, description, " +
                            "notes, organization_id, tenant_id, created_at, updated_at) VALUES(:mac_address, :uuid, " +
                            ":subsystem, :name, :description, :notes, :organization_id, :tenant_id, NOW(), NOW())")
                    .bind("mac_address", macAddress)
                    .bind("uuid", UUID.randomUUID())
                    .bind("subsystem", subsystem.name())
                    .bind("name", name)
                    .bind("description", description)
                    .bind("notes", notes)
                    .bind("organization_id", organizationId)
                    .bind("tenant_id", tenantId)
                    .execute()
        );
    }

    public List<MacAddressContextEntry> findAllMacAddressContext(UUID organizationId,
                                                                 UUID tenantId,
                                                                 int limit,
                                                                 int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM context_mac_addresses " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY mac_address ASC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(MacAddressContextEntry.class)
                        .list()
        );
    }

    public Long countMacAddressContext(UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM context_mac_addresses " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(Long.class)
                        .one()
        );
    }

}
