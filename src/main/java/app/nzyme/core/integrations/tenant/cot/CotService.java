package app.nzyme.core.integrations.tenant.cot;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.integrations.tenant.cot.db.CotOutputEntry;
import app.nzyme.core.integrations.tenant.cot.transports.CotTransportType;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CotService {

    private final NzymeNode nzyme;

    public CotService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public long countAllOutputsOfOrganization(UUID organizationId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM integrations_cot_outputs " +
                                "WHERE organization_id = :organization_id")
                        .bind("organization_id", organizationId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countAllOutputsOfTenant(UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM integrations_cot_outputs " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<CotOutputEntry> findAllOutputsOfTenant(UUID organizationId, UUID tenantId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM integrations_cot_outputs " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY name ASC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(CotOutputEntry.class)
                        .list()
        );
    }

    public List<CotOutputEntry> findAllOutputsOfAllTenants() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM integrations_cot_outputs")
                        .mapTo(CotOutputEntry.class)
                        .list()
        );
    }

    public Optional<CotOutputEntry> findOutput(UUID organizationId, UUID tenantId, UUID id) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM integrations_cot_outputs " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id AND uuid = :uuid")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("uuid", id)
                        .mapTo(CotOutputEntry.class)
                        .findOne()
        );
    }

    public void createOutput(UUID organizationId,
                             UUID tenantId,
                             CotTransportType connectionType,
                             String name,
                             String description,
                             String leafTypeTap,
                             String address,
                             int port,
                             @Nullable byte[] certificate) {
        if (description != null && description.trim().isEmpty()) {
            description = null;
        }

        String finalDescription = description;
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO integrations_cot_outputs(uuid, organization_id, tenant_id, " +
                                "connection_type, name, description, leaf_type_tap,  address, port, certificate, " +
                                "updated_at, created_at) VALUES(:uuid, :organization_id, :tenant_id, " +
                                ":connection_type, :name, :description, :leaf_type_tap, :address, :port, " +
                                ":certificate, NOW(), NOW())")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("connection_type", connectionType)
                        .bind("name", name)
                        .bind("description", finalDescription)
                        .bind("leaf_type_tap", leafTypeTap)
                        .bind("address", address)
                        .bind("port", port)
                        .bind("certificate", certificate)
                        .execute()
        );
    }

    public void updateOutput(long id,
                             CotTransportType connectionType,
                             String name,
                             String description,
                             String leafTypeTap,
                             String address,
                             int port,
                             @Nullable byte[] certificate) {
        if (description != null && description.trim().isEmpty()) {
            description = null;
        }

        String finalDescription = description;
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE integrations_cot_outputs SET connection_type = :connection_type, " +
                                "name = :name, description = :description, leaf_type_tap = :leaf_type_tap, " +
                                "address = :address, port = :port, certificate = :certificate, updated_at = NOW() " +
                                "WHERE id = :id")
                        .bind("id", id)
                        .bind("connection_type", connectionType)
                        .bind("name", name)
                        .bind("description", finalDescription)
                        .bind("leaf_type_tap", leafTypeTap)
                        .bind("address", address)
                        .bind("port", port)
                        .bind("certificate", certificate)
                        .execute()
        );
    }

    public void deleteOutput(long id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM integrations_cot_outputs WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public void pauseOutput(long id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE integrations_cot_outputs SET status = 'PAUSED' WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public void startOutput(long id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE integrations_cot_outputs SET status = 'RUNNING' WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

}
