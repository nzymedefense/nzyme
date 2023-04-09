package app.nzyme.core.security.authentication;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;

public class AuthenticationService {

    private static final Logger LOG = LogManager.getLogger(AuthenticationService.class);

    public final NzymeNode nzyme;

    public AuthenticationService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        seedDatabase();
    }

    private void seedDatabase() {
        long orgCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_organizations")
                        .mapTo(Long.class)
                        .one()
        );

        if (orgCount > 0) {
            return;
        }

        LOG.info("Creating default organization and tenant.");

        OrganizationEntry organization = createOrganization("Default Organization", "The nzyme default organization");
        createTenant(organization.id(), "Default Tenant", "The nzyme default tenant");

        LOG.info(organization);
    }

    public OrganizationEntry createOrganization(String name, String description) {
        // TODO AUDIT

        DateTime now = DateTime.now();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_organizations(name, description, created_at, updated_at) " +
                                "VALUES(:name, :description, :created_at, :updated_at) RETURNING *")
                        .bind("name", name)
                        .bind("description", description)
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .mapTo(OrganizationEntry.class)
                        .one()
        );
    }

    public List<OrganizationEntry> findAllOrganizations() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, name, description, created_at, updated_at FROM auth_organizations " +
                                "ORDER BY created_at ASC")
                        .mapTo(OrganizationEntry.class)
                        .list()
        );
    }

    public TenantEntry createTenant(long organizationId, String name, String description) {
        // TODO AUDIT

        DateTime now = DateTime.now();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_tenants(organization_id, name, description, created_at, updated_at) " +
                                "VALUES(:organization_id, :name, :description, :created_at, :updated_at) RETURNING *")
                        .bind("organization_id", organizationId)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .mapTo(TenantEntry.class)
                        .one()
        );
    }

    public Optional<List<TenantEntry>> findAllTenantsOfOrganization(long organizationId) {
        List<TenantEntry> tenants = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, organization_id, name, description, created_at, updated_at " +
                                "FROM auth_tenants WHERE organization_id = :organization_id ORDER BY created_at ASC")
                        .bind("organization_id", organizationId)
                        .mapTo(TenantEntry.class)
                        .list()
        );

        if (tenants.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(tenants);
        }
    }

}
