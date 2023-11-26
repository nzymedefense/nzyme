package app.nzyme.core.context;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.context.db.MacAddressContextEntry;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContextService {

    private final NzymeNode nzyme;

    public ContextService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void createMacAddressContext(String macAddress,
                                        String name,
                                        @Nullable String description,
                                        @Nullable String notes,
                                        UUID organizationId,
                                        UUID tenantId) {
        nzyme.getDatabase().useHandle(handle ->
            handle.createUpdate("INSERT INTO context_mac_addresses(mac_address, uuid, name, description, " +
                            "notes, organization_id, tenant_id, created_at, updated_at) VALUES(:mac_address, :uuid, " +
                            ":name, :description, :notes, :organization_id, :tenant_id, NOW(), NOW())")
                    .bind("mac_address", macAddress.toUpperCase())
                    .bind("uuid", UUID.randomUUID())
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

    public Optional<MacAddressContextEntry> findMacAddressContext(String mac,
                                                                  @Nullable UUID organizationId,
                                                                  @Nullable UUID tenantId) {
        if (organizationId != null && tenantId != null) {
            // Tenant data.
            return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM context_mac_addresses " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND mac_address = :mac_address")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("mac_address", mac)
                        .mapTo(MacAddressContextEntry.class)
                        .findOne()
            );
        }

        if (organizationId != null) {
            // Organization data.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT * FROM context_mac_addresses " +
                                    "WHERE organization_id = :organization_id " +
                                    "AND mac_address = :mac_address")
                            .bind("organization_id", organizationId)
                            .bind("mac_address", mac)
                            .mapTo(MacAddressContextEntry.class)
                            .findOne()
            );
        }

        // Any data.
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM context_mac_addresses " +
                                "WHERE mac_address = :mac_address")
                        .bind("mac_address", mac)
                        .mapTo(MacAddressContextEntry.class)
                        .findOne()
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
