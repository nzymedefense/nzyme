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

    public long countAllOrganizations() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_organizations")
                        .mapTo(Long.class)
                        .one()
        );
    }

    public Optional<OrganizationEntry> findOrganization(long id) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, name, description, created_at, updated_at FROM auth_organizations " +
                                "WHERE id = :id")
                        .bind("id", id)
                        .mapTo(OrganizationEntry.class)
                        .findOne()
        );
    }

    public void updateOrganization(long id, String name, String description) {
        Optional<OrganizationEntry> org = findOrganization(id);

        if (org.isEmpty()) {
            throw new RuntimeException("Organization with ID <" + id + "> does not exist.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_organizations SET name = :name, description = :description, " +
                                "updated_at = :now WHERE id = :id")
                        .bind("name", name)
                        .bind("description", description)
                        .bind("now", DateTime.now())
                        .bind("id", id)
                        .execute()
        );
    }

    public void deleteOrganization(long id) {
        Optional<OrganizationEntry> org = findOrganization(id);

        if (org.isEmpty()) {
            throw new RuntimeException("Organization with ID <" + id + "> does not exist.");
        }

        if (!isOrganizationDeletable(org.get())) {
            throw new RuntimeException("Organization with ID <" + id + "> cannot be deleted. Cannot have tenants and " +
                    "cannot be last remaining organization.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_organizations WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public boolean isOrganizationDeletable(OrganizationEntry org) {
        long organizationTenantCount = nzyme.getAuthenticationService().findAllTenantsOfOrganization(org.id())
                .map(List::size)
                .orElse(0);
        long totalOrganizationsCount = nzyme.getAuthenticationService().countAllOrganizations();

        return organizationTenantCount == 0 && totalOrganizationsCount > 1;
    }

    public TenantEntry createTenant(long organizationId, String name, String description) {
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

    public Optional<TenantEntry> findTenant(long tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, organization_id, name, description, created_at, updated_at " +
                                "FROM auth_tenants WHERE id = :id")
                        .bind("id", tenantId)
                        .mapTo(TenantEntry.class)
                        .findOne()
        );
    }

    public void updateTenant(long id, String name, String description) {
        Optional<TenantEntry> tenant = findTenant(id);

        if (tenant.isEmpty()) {
            throw new RuntimeException("Tenant with ID <" + id + "> does not exist.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_tenants SET name = :name, description = :description, " +
                                "updated_at = :now WHERE id = :id")
                        .bind("name", name)
                        .bind("description", description)
                        .bind("now", DateTime.now())
                        .bind("id", id)
                        .execute()
        );
    }

    public void deleteTenant(long id) {
        Optional<TenantEntry> tenant = findTenant(id);

        if (tenant.isEmpty()) {
            throw new RuntimeException("Tenant with ID <" + id + "> does not exist.");
        }

        if (!isTenantDeletable(tenant.get())) {
            throw new RuntimeException("Tenant with ID <" + id + "> cannot be deleted. Cannot have users in it.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_tenants WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public void createUserOfTenant(long organizationId,
                                   long tenantId,
                                   String name,
                                   String email,
                                   PasswordHasher.GeneratedHashAndSalt password) {
        // Check if this tenant already has a user with the same email address.
        if (tenantUserWithEmailExists(organizationId, tenantId, email)) {
            throw new RuntimeException("Tenant already has a user with same email address.");
        }

        DateTime now = new DateTime();
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO auth_users(organization_id, tenant_id, role_id, email, password, " +
                                "password_salt, name, created_at, updated_at, is_superadmin, is_orgadmin) " +
                                "VALUES(:organization_id, :tenant_id, NULL, :email, :password, :password_salt, :name, " +
                                ":created_at, :updated_at, false, false)")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("email", email)
                        .bind("password", password.hash())
                        .bind("password_salt", password.salt())
                        .bind("name", name)
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .execute()
        );
    }

    public boolean tenantUserWithEmailExists(long organizationId, long tenantId, String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("NULL or empty email address.");
        }

        Long count = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_users WHERE organization_id = :organization_id AND " +
                                "tenant_id = :tenant_id AND email = :email")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("email", email.toLowerCase())
                        .mapTo(Long.class)
                        .one()
        );

        return count > 0;
    }

    public boolean isTenantDeletable(TenantEntry t) {
        // TODO check if tenant has users.
        return true;
    }
}
