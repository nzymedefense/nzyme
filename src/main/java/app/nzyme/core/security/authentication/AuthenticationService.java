package app.nzyme.core.security.authentication;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.security.authentication.db.UserEntry;
import com.google.common.io.BaseEncoding;
import io.netty.handler.codec.base64.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
                                "ORDER BY name ASC")
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
                                "updated_at = NOW() WHERE id = :id")
                        .bind("name", name)
                        .bind("description", description)
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

    public long countTenantsOfOrganization(OrganizationEntry o) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_tenants " +
                                "WHERE organization_id = :organization_id")
                        .bind("organization_id", o.id())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countUsersOfOrganization(OrganizationEntry o) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_users " +
                                "WHERE organization_id = :organization_id")
                        .bind("organization_id", o.id())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countTapsOfOrganization(OrganizationEntry o) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM taps " +
                                "WHERE organization_id = :organization_id")
                        .bind("organization_id", o.id())
                        .mapTo(Long.class)
                        .one()
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
                                "FROM auth_tenants WHERE organization_id = :organization_id ORDER BY name DESC")
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
                                "updated_at = NOW() WHERE id = :id")
                        .bind("name", name)
                        .bind("description", description)
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

    public Optional<UserEntry> findUserOfTenant(long organizationId, long tenantId, long userId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, organization_id, tenant_id, role_id, email, name, is_orgadmin, " +
                                "is_superadmin, updated_at, created_at FROM auth_users " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND id = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("user_id", userId)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public List<UserEntry> findAllUsersOfTenant(long organizationId, long tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, organization_id, tenant_id, role_id, email, name, is_orgadmin, " +
                                "is_superadmin, updated_at, created_at FROM auth_users " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY name ASC")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(UserEntry.class)
                        .list()
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

    public void editUserOfTenant(long organizationId, long tenantId, long userId, String name, String email) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET name = :name, email = :email, updated_at = NOW() " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id AND id = :user_id")
                        .bind("name", name)
                        .bind("email", email)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("user_id", userId)
                        .execute()
        );
    }


    public void editUserOfTenantPassword(long organizationId, long tenantId, long userId, PasswordHasher.GeneratedHashAndSalt password) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET password = :password, password_salt = :password_salt, " +
                        "updated_at = NOW() WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                        "AND id = :user_id")
                        .bind("password", password.hash())
                        .bind("password_salt", password.salt())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void deleteUserOfTenant(long organizationId, long tenantId, long userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_users " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id AND id = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public long countUsersOfTenant(TenantEntry t) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_users WHERE organization_id = :organization_id AND " +
                                "tenant_id = :tenant_id")
                        .bind("organization_id", t.organizationId())
                        .bind("tenant_id", t.id())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countTapsOfTenant(TenantEntry t) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM taps WHERE organization_id = :organization_id AND " +
                                "tenant_id = :tenant_id")
                        .bind("organization_id", t.organizationId())
                        .bind("tenant_id", t.id())
                        .mapTo(Long.class)
                        .one()
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

    public TapPermissionEntry createTap(long organizationId, long tenantId, String secret, String name, String description) {
        String encryptedSecret;
        try {
            encryptedSecret = BaseEncoding.base64().encode(nzyme.getCrypto().encryptWithClusterKey(secret.getBytes()));
        } catch (Crypto.CryptoOperationException e) {
            throw new RuntimeException("Could not encrypt tap secret.", e);
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO taps(uuid, organization_id, tenant_id, secret, name, " +
                                "description, deleted, created_at, updated_at) VALUES(:uuid, :organization_id, :tenant_id, " +
                                ":secret, :name, :description, false, :created_at, :updated_at) RETURNING *")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("secret", encryptedSecret)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("created_at", DateTime.now())
                        .bind("updated_at", DateTime.now())
                        .mapTo(TapPermissionEntry.class)
                        .one()
        );
    }

    public List<TapPermissionEntry> findAllTaps(long organizationId, long tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT uuid, organization_id, tenant_id, name, " +
                                "description, secret, created_at, updated_at, last_report FROM taps " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY name ASC")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(TapPermissionEntry.class)
                        .list()
        );
    }

    public Optional<TapPermissionEntry> findTap(long organizationId, long tenantId, UUID tapId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT uuid, organization_id, tenant_id, name, " +
                                "description, secret, created_at, updated_at, last_report FROM taps " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND uuid = :uuid")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("uuid", tapId)
                        .mapTo(TapPermissionEntry.class)
                        .findOne()
        );
    }

    public Optional<TapPermissionEntry> findTapBySecret(String secret) {
        /*
         * We have to pull all taps here and then loop over them because the secret is encrypted, and we can't use
         * a SELECT WHERE with a non-deterministic encryption like PGP.
         */

        List<TapPermissionEntry> taps = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT uuid, organization_id, tenant_id, name, description, secret, " +
                                "created_at, updated_at, last_report FROM taps")
                        .mapTo(TapPermissionEntry.class)
                        .list()
        );

        for (TapPermissionEntry tap : taps) {
            try {
                String decryptedSecret = new String(
                        nzyme.getCrypto().decryptWithClusterKey(
                                BaseEncoding.base64().decode(tap.secret())
                        )
                );

                if (secret.equals(decryptedSecret)) {
                    return Optional.of(tap);
                }
            } catch (Crypto.CryptoOperationException e) {
                throw new RuntimeException("Could not decrypt tap key.", e);
            }
        }

        return Optional.empty();
    }

    public void deleteTap(long organizationId, long tenantId, UUID tapId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM taps WHERE organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id AND uuid = :uuid")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("uuid", tapId)
                        .execute()
        );
    }

    public void editTap(long organizationId, long tenantId, UUID tapId, String name, String description) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE taps SET name = :name, description = :description, updated_at = NOW() " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id AND uuid = :uuid")
                        .bind("name", name)
                        .bind("description", description)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("uuid", tapId)
                        .execute()
        );    }

    public void cycleTapSecret(long organizationId, long tenantId, UUID tapId, String newSecret) {
        String encryptedSecret;
        try {
            encryptedSecret = BaseEncoding.base64().encode(nzyme.getCrypto().encryptWithClusterKey(newSecret.getBytes()));
        } catch (Crypto.CryptoOperationException e) {
            throw new RuntimeException("Could not encrypt tap secret.", e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE taps SET secret = :secret, updated_at = NOW() " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id AND uuid = :uuid")
                        .bind("secret", encryptedSecret)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("uuid", tapId)
                        .execute()
        );
    }

    public boolean isTenantDeletable(TenantEntry t) {
        return countTapsOfTenant(t) == 0 && countUsersOfTenant(t) == 0;
    }

}
