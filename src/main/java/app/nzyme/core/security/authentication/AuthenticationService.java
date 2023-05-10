package app.nzyme.core.security.authentication;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.integrations.geoip.GeoIpLookupResult;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.core.security.sessions.db.SessionEntry;
import app.nzyme.core.security.sessions.db.SessionEntryWithUserDetails;
import com.google.common.io.BaseEncoding;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthenticationService {

    private static final Logger LOG = LogManager.getLogger(AuthenticationService.class);

    public static final int MFA_ENTRY_TIME_MINUTES = 5;

    public final NzymeNode nzyme;

    public AuthenticationService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        seedDatabase();

        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("session-cleaner-%d")
                        .build()
        ).scheduleAtFixedRate(this::runSessionCleaning, 0, 1, TimeUnit.MINUTES);
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

    public long countSuperAdministrators() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_users WHERE is_superadmin = true")
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<UserEntry> findAllSuperAdministrators(int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE is_superadmin = true " +
                                "ORDER BY name ASC LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(UserEntry.class)
                        .list()
        );
    }

    public Optional<UserEntry> findSuperAdministrator(long userId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE is_superadmin = true AND id = :user_id")
                        .bind("user_id", userId)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public UserEntry createSuperAdministrator(String name, String email,PasswordHasher.GeneratedHashAndSalt password) {
        DateTime now = new DateTime();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_users(organization_id, tenant_id, role_id, email, password, " +
                                "password_salt, name, created_at, updated_at, is_superadmin, is_orgadmin) " +
                                "VALUES(NULL, NULL, NULL, :email, :password, :password_salt, :name, " +
                                ":created_at, :updated_at, true, false) RETURNING *")
                        .bind("email", email)
                        .bind("password", password.hash())
                        .bind("password_salt", password.salt())
                        .bind("name", name)
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .mapTo(UserEntry.class)
                        .one()
        );
    }

    public void editSuperAdministrator(long userId, String name, String email) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET name = :name, email = :email, updated_at = NOW() " +
                                "WHERE is_superadmin = true AND id = :user_id")
                        .bind("name", name)
                        .bind("email", email)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void editSuperAdministratorPassword(long userId, PasswordHasher.GeneratedHashAndSalt password) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET password = :password, password_salt = :password_salt, " +
                                "updated_at = NOW() WHERE is_superadmin = true AND id = :user_id")
                        .bind("password", password.hash())
                        .bind("password_salt", password.salt())
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void deleteSuperAdministrator(long userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_users WHERE is_superadmin = true AND id = :user_id")
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void resetMFAOfSuperAdministrator(long userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET mfa_complete = false, " +
                                "totp_secret = NULL, mfa_recovery_codes = NULL " +
                                "WHERE is_superadmin = true AND id = :user_id")
                        .bind("user_id", userId)
                        .execute()
        );

        // Reset all sessions of this user.
        deleteAllSessionsOfUser(userId);
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

    public List<OrganizationEntry> findAllOrganizations(int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, name, description, created_at, updated_at FROM auth_organizations " +
                                "ORDER BY name ASC LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset)
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
        long organizationTenantCount = countTenantsOfOrganization(org);
        long totalOrganizationsCount = countAllOrganizations();

        return organizationTenantCount == 0 && totalOrganizationsCount > 1;
    }

    public List<UserEntry> findAllOrganizationAdministrators(long organizationId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE is_orgadmin = true AND organization_id = :organization_id " +
                                "ORDER BY name ASC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(UserEntry.class)
                        .list()
        );
    }

    public Optional<UserEntry> findOrganizationAdministrator(long organizationId, long userId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id AND id = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("user_id", userId)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public long countOrganizationAdministrators(long organizationId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_users " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id")
                        .bind("organization_id", organizationId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public UserEntry createOrganizationAdministrator(long organizationId,
                                                     String name,
                                                     String email,
                                                     PasswordHasher.GeneratedHashAndSalt password) {
        DateTime now = new DateTime();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_users(organization_id, tenant_id, role_id, email, password, " +
                                "password_salt, name, created_at, updated_at, is_superadmin, is_orgadmin) " +
                                "VALUES(:organization_id, NULL, NULL, :email, :password, :password_salt, :name, " +
                                ":created_at, :updated_at, false, true) RETURNING *")
                        .bind("organization_id", organizationId)
                        .bind("email", email)
                        .bind("password", password.hash())
                        .bind("password_salt", password.salt())
                        .bind("name", name)
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .mapTo(UserEntry.class)
                        .one()
        );
    }

    public void editOrganizationAdministrator(long organizationId, long userId, String name, String email) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET name = :name, email = :email, updated_at = NOW() " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id AND id = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("name", name)
                        .bind("email", email)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void editOrganizationAdministratorPassword(long organizationId,
                                                      long userId,
                                                      PasswordHasher.GeneratedHashAndSalt password) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET password = :password, password_salt = :password_salt, " +
                                "updated_at = NOW() " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id AND id = :user_id")
                        .bind("password", password.hash())
                        .bind("password_salt", password.salt())
                        .bind("organization_id", organizationId)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void deleteOrganizationAdministrator(long organizationId, long userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_users " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id AND id = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void resetMFAOfOrganizationAdministrator(long organizationId, long userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET mfa_complete = false, " +
                                "totp_secret = NULL, mfa_recovery_codes = NULL " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id AND id = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("user_id", userId)
                        .execute()
        );

        // Reset all sessions of this user.
        deleteAllSessionsOfUser(userId);
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

    public Optional<List<TenantEntry>> findAllTenantsOfOrganization(long organizationId, int limit, int offset) {
        List<TenantEntry> tenants = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, organization_id, name, description, created_at, updated_at " +
                                "FROM auth_tenants WHERE organization_id = :organization_id " +
                                "ORDER BY name DESC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("limit", limit)
                        .bind("offset", offset)
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
                handle.createQuery("SELECT * FROM auth_users WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND id = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("user_id", userId)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public List<UserEntry> findAllUsersOfTenant(long organizationId, long tenantId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY name ASC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(UserEntry.class)
                        .list()
        );
    }

    public Optional<UserEntry> findUserByEmail(String email) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE email = :email")
                        .bind("email", email)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public Optional<UserEntry> findUserById(long id) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE id = :id")
                        .bind("id", id)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public UserEntry createUserOfTenant(long organizationId,
                                   long tenantId,
                                   String name,
                                   String email,
                                   PasswordHasher.GeneratedHashAndSalt password) {
        DateTime now = new DateTime();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_users(organization_id, tenant_id, role_id, email, password, " +
                                "password_salt, name, created_at, updated_at, is_superadmin, is_orgadmin) " +
                                "VALUES(:organization_id, :tenant_id, NULL, :email, :password, :password_salt, :name, " +
                                ":created_at, :updated_at, false, false) RETURNING *")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("email", email)
                        .bind("password", password.hash())
                        .bind("password_salt", password.salt())
                        .bind("name", name)
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .mapTo(UserEntry.class)
                        .one()
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

    public void resetMFAOfUserOfTenant(long organizationId, long tenantId, long userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET mfa_complete = false, " +
                        "totp_secret = NULL, mfa_recovery_codes = NULL " +
                        "WHERE organization_id = :organization_id AND tenant_id = :tenant_id AND id = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("user_id", userId)
                        .execute()
        );

        // Reset all sessions of this user.
        deleteAllSessionsOfUser(userId);
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

    public boolean userWithEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("NULL or empty email address.");
        }

        long count = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_users WHERE email = :email")
                        .bind("email", email.toLowerCase())
                        .mapTo(Long.class)
                        .one()
        );

        return count > 0;
    }

    public void setUserTOTPSecret(long userId, String secret) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET totp_secret = :secret WHERE id = :user_id")
                        .bind("secret", secret)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void setUserMFARecoveryCodes(long userId, String recoveryCodes) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET mfa_recovery_codes = :recovery_codes WHERE id = :user_id")
                        .bind("recovery_codes", recoveryCodes)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void setUserMFAComplete(long userId, boolean complete) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET mfa_complete = :mfa_complete WHERE id = :user_id")
                        .bind("mfa_complete", complete)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void createSession(String sessionId, long userId, String remoteIp) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO auth_sessions(sessionid, user_id, remote_ip, created_at, " +
                                "mfa_valid, mfa_requested_at) VALUES(:sessionid, :user_id, :remote_ip, NOW(), " +
                                "false, NOW())")
                        .bind("sessionid", sessionId)
                        .bind("user_id", userId)
                        .bind("remote_ip", remoteIp)
                        .execute()
        );
    }

    public List<SessionEntryWithUserDetails> findAllSessions(int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.id, s.sessionid, s.user_id, s.remote_ip, s.created_at, u.last_activity, " +
                                "u.tenant_id, u.organization_id, u.email, u.name, u.is_superadmin, u.is_orgadmin, " +
                                "s.mfa_valid, s.mfa_requested_at " +
                                "FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.id " +
                                "ORDER BY u.email ASC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(SessionEntryWithUserDetails.class)
                        .list()
        );
    }

    public List<SessionEntryWithUserDetails> findSessionsOfOrganization(long organizationId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.id, s.sessionid, s.user_id, s.remote_ip, s.created_at, u.last_activity, " +
                                "u.tenant_id, u.organization_id, u.email, u.name, u.is_superadmin, u.is_orgadmin, " +
                                "s.mfa_valid, s.mfa_requested_at " +
                                "FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.id " +
                                "WHERE u.organization_id = :organization_id " +
                                "ORDER BY u.email ASC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(SessionEntryWithUserDetails.class)
                        .list()
        );
    }

    public List<SessionEntryWithUserDetails> findSessionsOfTenant(long organizationId, long tenantId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.id, s.sessionid, s.user_id, s.remote_ip, s.created_at, u.last_activity, " +
                                "u.tenant_id, u.organization_id, u.email, u.name, u.is_superadmin, u.is_orgadmin, " +
                                "s.mfa_valid, s.mfa_requested_at " +
                                "FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.id " +
                                "WHERE u.organization_id = :organization_id AND u.tenant_id = :tenant_id " +
                                "ORDER BY u.email ASC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(SessionEntryWithUserDetails.class)
                        .list()
        );
    }

    public Optional<SessionEntry> findSessionWithOrWithoutPassedMFABySessionId(String sessionId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT sessionid, user_id, remote_ip, created_at, elevated, elevated_since, " +
                                "mfa_valid, mfa_requested_at FROM auth_sessions WHERE sessionid = :sessionid")
                        .bind("sessionid", sessionId)
                        .mapTo(SessionEntry.class)
                        .findOne()
        );
    }

    public Optional<SessionEntry> findSessionWithPassedMFABySessionId(String sessionId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT sessionid, user_id, remote_ip, created_at, elevated, elevated_since, " +
                                "mfa_valid, mfa_requested_at " +
                                "FROM auth_sessions WHERE sessionid = :sessionid AND mfa_valid = true")
                        .bind("sessionid", sessionId)
                        .mapTo(SessionEntry.class)
                        .findOne()
        );
    }

    public void markSessionAsMFAValid(String sessionId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_sessions SET mfa_valid = true, mfa_requested_at = NULL " +
                                "WHERE sessionid = :sessionid")
                        .bind("sessionid", sessionId)
                        .execute()
        );
    }

    public void deleteSession(long id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_sessions WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public long countAllSessions() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_sessions")
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countSessionsOfOrganization(long organizationId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.id " +
                                "WHERE u.organization_id = :organization_id")
                        .bind("organization_id", organizationId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countSessionsOfTenant(long organizationId, long tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.id " +
                                "WHERE u.organization_id = :organization_id AND u.tenant_id = :tenant_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public void updateLastUserActivity(long userId, String remoteIp, @Nullable GeoIpLookupResult remoteIpGeo) {
        String countryCode = remoteIpGeo != null && remoteIpGeo.geo() != null ? remoteIpGeo.geo().countryCode() : null;
        String city = remoteIpGeo != null && remoteIpGeo.geo() != null ? remoteIpGeo.geo().city() : null;
        String asnName = remoteIpGeo != null && remoteIpGeo.asn() != null ? remoteIpGeo.asn().name() : null;;

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET last_activity = NOW(), last_remote_ip = :remote_ip, " +
                                "last_geo_country = :country_code, last_geo_city = :city, last_geo_asn = :asn " +
                                "WHERE id = :user_id")
                        .bind("remote_ip", remoteIp)
                        .bind("country_code", countryCode)
                        .bind("city", city)
                        .bind("asn", asnName)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void deleteAllSessionsOfUser(long userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_sessions WHERE user_id = :user_id")
                        .bind("user_id", userId)
                        .execute()
        );
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

    public List<TapPermissionEntry> findAllTaps(long organizationId, long tenantId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT uuid, organization_id, tenant_id, name, " +
                                "description, secret, created_at, updated_at, last_report FROM taps " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY name ASC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
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

    private void runSessionCleaning() {
        try {
            // Delete all sessions older than 12 hours.
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("DELETE FROM auth_sessions WHERE created_at < :timeout")
                            .bind("timeout", DateTime.now().minusHours(12))
                            .execute()
            );

            // Delete all sessions with MFA waiting for longer than 5 minutes.
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("DELETE FROM auth_sessions " +
                                    "WHERE mfa_valid = false AND mfa_requested_at < :timeout")
                            .bind("timeout", DateTime.now().minusMinutes(MFA_ENTRY_TIME_MINUTES))
                            .execute()
            );

            // Delete all sessions of users that have been inactive for 15 minutes.
            List<Long> inactiveUsers = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT u.id FROM auth_users AS u " +
                                    "LEFT JOIN auth_sessions AS s ON s.user_id = u.id " +
                                    "WHERE s.mfa_valid = true AND u.last_activity < :timeout")
                            .bind("timeout", DateTime.now().minusMinutes(15))
                            .mapTo(Long.class)
                            .list()
            );

            if (!inactiveUsers.isEmpty()) {
                nzyme.getDatabase().useHandle(handle ->
                        handle.createUpdate("DELETE FROM auth_sessions WHERE user_id IN (<user_ids>)")
                                .bindList("user_ids", inactiveUsers)
                                .execute()
                );
            }
        } catch(Exception e) {
            LOG.error("Could not run session cleaning.", e);
        }
    }

    public boolean isTenantDeletable(TenantEntry t) {
        return countTapsOfTenant(t) == 0 && countUsersOfTenant(t) == 0;
    }

}