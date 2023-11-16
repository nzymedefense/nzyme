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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthenticationService {

    private static final Logger LOG = LogManager.getLogger(AuthenticationService.class);

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
        ).scheduleAtFixedRate(this::runSessionCleaning, 0, 30, TimeUnit.SECONDS);
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

        OrganizationEntry organization = createOrganization(
                "Default Organization",
                "The nzyme default organization"
        );
        createTenant(organization.uuid(),
                "Default Tenant",
                "The nzyme default tenant",
                Integer.parseInt(AuthenticationRegistryKeys.SESSION_TIMEOUT_MINUTES.defaultValue().get()),
                Integer.parseInt(AuthenticationRegistryKeys.SESSION_INACTIVITY_TIMEOUT_MINUTES.defaultValue().get()),
                Integer.parseInt(AuthenticationRegistryKeys.MFA_TIMEOUT_MINUTES.defaultValue().get())
        );
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

    public Optional<UserEntry> findSuperAdministrator(UUID userId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE is_superadmin = true AND uuid = :user_id")
                        .bind("user_id", userId)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public UserEntry createSuperAdministrator(String name, String email,PasswordHasher.GeneratedHashAndSalt password) {
        DateTime now = new DateTime();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_users(organization_id, tenant_id, email, password, " +
                                "password_salt, name, created_at, updated_at, is_superadmin, is_orgadmin) " +
                                "VALUES(NULL, NULL, :email, :password, :password_salt, :name, " +
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

    public void deleteSuperAdministrator(UUID userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_users WHERE is_superadmin = true AND uuid = :user_id")
                        .bind("user_id", userId)
                        .execute()
        );
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
        return findAllOrganizations(Integer.MAX_VALUE, 0);
    }

    public List<OrganizationEntry> findAllOrganizations(int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT uuid, name, description, created_at, updated_at FROM auth_organizations " +
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

    public Optional<OrganizationEntry> findOrganization(UUID id) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT uuid, name, description, created_at, updated_at FROM auth_organizations " +
                                "WHERE uuid = :id")
                        .bind("id", id)
                        .mapTo(OrganizationEntry.class)
                        .findOne()
        );
    }

    public void updateOrganization(UUID id, String name, String description) {
        Optional<OrganizationEntry> org = findOrganization(id);

        if (org.isEmpty()) {
            throw new RuntimeException("Organization with ID <" + id + "> does not exist.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_organizations SET name = :name, description = :description, " +
                                "updated_at = NOW() WHERE uuid = :id")
                        .bind("name", name)
                        .bind("description", description)
                        .bind("id", id)
                        .execute()
        );
    }

    public void deleteOrganization(UUID id) {
        Optional<OrganizationEntry> org = findOrganization(id);

        if (org.isEmpty()) {
            throw new RuntimeException("Organization with ID <" + id + "> does not exist.");
        }

        if (!isOrganizationDeletable(org.get())) {
            throw new RuntimeException("Organization with ID <" + id + "> cannot be deleted. Cannot have tenants and " +
                    "cannot be last remaining organization.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_organizations WHERE uuid = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public long countTenantsOfOrganization(OrganizationEntry o) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_tenants " +
                                "WHERE organization_id = :organization_id")
                        .bind("organization_id", o.uuid())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countUsersOfOrganization(OrganizationEntry o) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_users " +
                                "WHERE organization_id = :organization_id")
                        .bind("organization_id", o.uuid())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countTapsOfOrganization(OrganizationEntry o) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM taps " +
                                "WHERE organization_id = :organization_id")
                        .bind("organization_id", o.uuid())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public boolean isOrganizationDeletable(OrganizationEntry org) {
        long organizationTenantCount = countTenantsOfOrganization(org);
        long totalOrganizationsCount = countAllOrganizations();

        return organizationTenantCount == 0 && totalOrganizationsCount > 1;
    }

    public List<UserEntry> findAllOrganizationAdministrators(UUID organizationId, int limit, int offset) {
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

    public Optional<UserEntry> findOrganizationAdministrator(UUID organizationId, UUID userId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id AND uuid = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("user_id", userId)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public long countOrganizationAdministrators(UUID organizationId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_users " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id")
                        .bind("organization_id", organizationId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public UserEntry createOrganizationAdministrator(UUID organizationId,
                                                     String name,
                                                     String email,
                                                     PasswordHasher.GeneratedHashAndSalt password) {
        DateTime now = new DateTime();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_users(organization_id, tenant_id, email, password, " +
                                "password_salt, name, created_at, updated_at, is_superadmin, is_orgadmin) " +
                                "VALUES(:organization_id, NULL, :email, :password, :password_salt, :name, " +
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

    public void deleteOrganizationAdministrator(UUID organizationId, UUID userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_users " +
                                "WHERE is_orgadmin = true AND organization_id = :organization_id AND uuid = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public TenantEntry createTenant(UUID organizationId,
                                    String name,
                                    String description,
                                    int sessionTimeoutMinutes,
                                    int sessionInactivityTimeoutMinutes,
                                    int mfaTimeoutMinutes) {
        DateTime now = DateTime.now();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_tenants(organization_id, name, description, " +
                                "session_timeout_minutes, session_inactivity_timeout_minutes, mfa_timeout_minutes, " +
                                "created_at, updated_at) VALUES(:organization_id, :name, :description, " +
                                ":session_timeout_minutes, :session_inactivity_timeout_minutes, :mfa_timeout_minutes, " +
                                ":created_at, :updated_at) RETURNING *")
                        .bind("organization_id", organizationId)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("session_timeout_minutes", sessionTimeoutMinutes)
                        .bind("session_inactivity_timeout_minutes", sessionInactivityTimeoutMinutes)
                        .bind("mfa_timeout_minutes", mfaTimeoutMinutes)
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .mapTo(TenantEntry.class)
                        .one()
        );
    }

    public Optional<List<TenantEntry>> findAllTenantsOfOrganization(UUID organizationId) {
        return findAllTenantsOfOrganization(organizationId, Integer.MAX_VALUE, 0);
    }

    public Optional<List<TenantEntry>> findAllTenantsOfOrganization(UUID organizationId, int limit, int offset) {
        List<TenantEntry> tenants = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_tenants " +
                                "WHERE organization_id = :organization_id " +
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

    public Optional<TenantEntry> findTenant(UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_tenants WHERE uuid = :id")
                        .bind("id", tenantId)
                        .mapTo(TenantEntry.class)
                        .findOne()
        );
    }

    public void updateTenant(UUID id,
                             String name,
                             String description,
                             int sessionTimeoutMinutes,
                             int sessionInactivityTimeoutMinutes,
                             int mfaTimeoutMinutes) {
        Optional<TenantEntry> tenant = findTenant(id);

        if (tenant.isEmpty()) {
            throw new RuntimeException("Tenant with ID <" + id + "> does not exist.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_tenants SET name = :name, description = :description, " +
                                "session_timeout_minutes = :session_timeout_minutes, " +
                                "session_inactivity_timeout_minutes = :session_inactivity_timeout_minutes, " +
                                "mfa_timeout_minutes = :mfa_timeout_minutes, updated_at = NOW() WHERE uuid = :id")
                        .bind("name", name)
                        .bind("description", description)
                        .bind("session_timeout_minutes", sessionTimeoutMinutes)
                        .bind("session_inactivity_timeout_minutes", sessionInactivityTimeoutMinutes)
                        .bind("mfa_timeout_minutes", mfaTimeoutMinutes)
                        .bind("id", id)
                        .execute()
        );
    }

    public void deleteTenant(UUID id) {
        Optional<TenantEntry> tenant = findTenant(id);

        if (tenant.isEmpty()) {
            throw new RuntimeException("Tenant with ID <" + id + "> does not exist.");
        }

        if (!isTenantDeletable(tenant.get())) {
            throw new RuntimeException("Tenant with ID <" + id + "> cannot be deleted. Cannot have users in it.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_tenants WHERE uuid = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public Optional<UserEntry> findUserOfTenant(UUID organizationId, UUID tenantId, UUID userId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND uuid = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("user_id", userId)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public List<UserEntry> findAllUsersOfTenant(UUID organizationId, UUID tenantId, int limit, int offset) {
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

    public Optional<UserEntry> findUserById(UUID id) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM auth_users WHERE uuid = :id")
                        .bind("id", id)
                        .mapTo(UserEntry.class)
                        .findOne()
        );
    }

    public List<String> findPermissionsOfUser(UUID userId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT permission FROM auth_permissions WHERE user_id = :user_id")
                        .bind("user_id", userId)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<UUID> findTapPermissionsOfUser(UUID userId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT t.uuid FROM auth_users_taps AS u " +
                                "LEFT JOIN taps t on u.tap_id = t.uuid " +
                                "WHERE u.user_id = :user_id")
                        .bind("user_id", userId)
                        .mapTo(UUID.class)
                        .list()
        );
    }

    public void setUserTapPermissionsAllowAll(UUID userId, boolean allowAccessAllTenantTaps) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET access_all_tenant_taps = :state WHERE uuid = :user_id")
                        .bind("state", allowAccessAllTenantTaps)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void setUserTapPermissions(UUID userId, List<UUID> newPermissions) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_users_taps WHERE user_id = :user_id")
                        .bind("user_id", userId)
                        .execute()
        );

        for (UUID tapUuid : newPermissions) {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO auth_users_taps(user_id, tap_id) " +
                                    "VALUES(:user_id, :tap_id)")
                            .bind("user_id", userId)
                            .bind("tap_id", tapUuid)
                            .execute()
            );
        }
    }

    public void setUserPermissions(UUID userId, List<String> permissions) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_permissions WHERE user_id = :user_id")
                        .bind("user_id", userId)
                        .execute()
        );

        for (String permission : permissions) {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO auth_permissions(user_id, permission) " +
                                    "VALUES(:user_id, :permission)")
                            .bind("user_id", userId)
                            .bind("permission", permission)
                            .execute()
            );
        }
    }

    public UserEntry createUserOfTenant(UUID organizationId,
                                        UUID tenantId,
                                        String name,
                                        String email,
                                        PasswordHasher.GeneratedHashAndSalt password) {
        DateTime now = new DateTime();
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_users(organization_id, tenant_id, email, password, " +
                                "password_salt, name, created_at, updated_at, is_superadmin, is_orgadmin) " +
                                "VALUES(:organization_id, :tenant_id, :email, :password, :password_salt, :name, " +
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

    public void editUser(UUID userId, String name, String email) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET name = :name, email = :email, updated_at = NOW() " +
                                "WHERE uuid = :user_id")
                        .bind("name", name)
                        .bind("email", email)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void editUserPassword(UUID userId, PasswordHasher.GeneratedHashAndSalt password) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET password = :password, password_salt = :password_salt, " +
                        "updated_at = NOW() WHERE uuid = :user_id")
                        .bind("password", password.hash())
                        .bind("password_salt", password.salt())
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void deleteUserOfTenant(UUID organizationId, UUID tenantId, UUID userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_users " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id AND uuid = :user_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void resetMFAOfUser(UUID userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET mfa_complete = false, " +
                                "totp_secret = NULL, mfa_recovery_codes = NULL WHERE uuid = :user_id")
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
                        .bind("organization_id", t.organizationUuid())
                        .bind("tenant_id", t.uuid())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countTapsOfTenant(TenantEntry t) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM taps WHERE organization_id = :organization_id AND " +
                                "tenant_id = :tenant_id")
                        .bind("organization_id", t.organizationUuid())
                        .bind("tenant_id", t.uuid())
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

    public void setUserTOTPSecret(UUID userId, String secret) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET totp_secret = :secret WHERE uuid = :user_id")
                        .bind("secret", secret)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void setUserMFARecoveryCodes(UUID userId, String recoveryCodes) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET mfa_recovery_codes = :recovery_codes WHERE uuid = :user_id")
                        .bind("recovery_codes", recoveryCodes)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public Optional<Map<String, Boolean>> getUserMFARecoveryCodes(UUID userId) {
        Optional<String> result = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT mfa_recovery_codes FROM auth_users WHERE uuid = :user_id")
                        .bind("user_id", userId)
                        .mapTo(String.class)
                        .findOne()
        );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        byte[] encrypted = BaseEncoding.base64().decode(result.get());

        String json;
        try {
            json = new String(nzyme.getCrypto().decryptWithClusterKey(encrypted));
        } catch(Crypto.CryptoOperationException e) {
            throw new RuntimeException("Could not decrypt MFA recovery codes.", e);
        }

        try {
            ObjectMapper om = new ObjectMapper();
            return Optional.of(om.readValue(json, new TypeReference<>() {}));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse MFA recovery code JSON.", e);
        }
    }

    public void setUserMFAComplete(UUID userId, boolean complete) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET mfa_complete = :mfa_complete WHERE uuid = :user_id")
                        .bind("mfa_complete", complete)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void markUserFailedLogin(UserEntry user) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET failed_login_count = COALESCE(failed_login_count+1, 1) " +
                                "WHERE uuid = :user_id")
                        .bind("user_id", user.uuid())
                        .execute()
        );
    }

    public void markUserSuccessfulLogin(UserEntry user) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET failed_login_count = NULL WHERE uuid = :user_id")
                        .bind("user_id", user.uuid())
                        .execute()
        );
    }

    public void createSession(String sessionId, UUID userId, String remoteIp) {
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
                                "LEFT JOIN auth_users u ON s.user_id = u.uuid " +
                                "ORDER BY u.email ASC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(SessionEntryWithUserDetails.class)
                        .list()
        );
    }

    public List<SessionEntryWithUserDetails> findSessionsOfOrganization(UUID organizationId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.id, s.sessionid, s.user_id, s.remote_ip, s.created_at, u.last_activity, " +
                                "u.tenant_id, u.organization_id, u.email, u.name, u.is_superadmin, u.is_orgadmin, " +
                                "s.mfa_valid, s.mfa_requested_at " +
                                "FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.uuid " +
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

    public List<SessionEntryWithUserDetails> findSessionsOfTenant(UUID organizationId, UUID tenantId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.id, s.sessionid, s.user_id, s.remote_ip, s.created_at, u.last_activity, " +
                                "u.tenant_id, u.organization_id, u.email, u.name, u.is_superadmin, u.is_orgadmin, " +
                                "s.mfa_valid, s.mfa_requested_at " +
                                "FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.uuid " +
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

    public Optional<SessionEntry> findSessionWithOrWithoutPassedMFAById(long id) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT sessionid, user_id, remote_ip, created_at, elevated, elevated_since, " +
                                "mfa_valid, mfa_requested_at FROM auth_sessions WHERE id = :id")
                        .bind("id", id)
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

    public long countSessionsOfOrganization(UUID organizationId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.uuid " +
                                "WHERE u.organization_id = :organization_id")
                        .bind("organization_id", organizationId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countSessionsOfTenant(UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_sessions AS s " +
                                "LEFT JOIN auth_users u ON s.user_id = u.uuid " +
                                "WHERE u.organization_id = :organization_id AND u.tenant_id = :tenant_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public void updateLastUserActivity(UUID userId, String remoteIp, @Nullable GeoIpLookupResult remoteIpGeo) {
        String countryCode = remoteIpGeo != null && remoteIpGeo.geo() != null ? remoteIpGeo.geo().countryCode() : null;
        String city = remoteIpGeo != null && remoteIpGeo.geo() != null ? remoteIpGeo.geo().city() : null;
        String asnName = remoteIpGeo != null && remoteIpGeo.asn() != null ? remoteIpGeo.asn().name() : null;;

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE auth_users SET last_activity = NOW(), last_remote_ip = :remote_ip, " +
                                "last_geo_country = :country_code, last_geo_city = :city, last_geo_asn = :asn " +
                                "WHERE uuid = :user_id")
                        .bind("remote_ip", remoteIp)
                        .bind("country_code", countryCode)
                        .bind("city", city)
                        .bind("asn", asnName)
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public void deleteAllSessionsOfUser(UUID userId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_sessions WHERE user_id = :user_id")
                        .bind("user_id", userId)
                        .execute()
        );
    }

    public TapPermissionEntry createTap(UUID organizationId, UUID tenantId, String secret, String name, String description) {
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

    public List<TapPermissionEntry> findAllTapsOfTenant(UUID organizationId, UUID tenantId) {
        return findAllTapsOfTenant(organizationId, tenantId, Integer.MAX_VALUE, 0);
    }

    public List<TapPermissionEntry> findAllTapsOfTenant(UUID organizationId, UUID tenantId, int limit, int offset) {
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

    public Optional<TapPermissionEntry> findTap(UUID organizationId, UUID tenantId, UUID tapId) {
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

    public void deleteTap(UUID organizationId, UUID tenantId, UUID tapId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM taps WHERE organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id AND uuid = :uuid")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("uuid", tapId)
                        .execute()
        );

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM auth_users_taps WHERE tap_id = :uuid")
                        .bind("uuid", tapId)
                        .execute()
        );
    }

    public void editTap(UUID organizationId, UUID tenantId, UUID tapId, String name, String description) {
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

    public void cycleTapSecret(UUID organizationId, UUID tenantId, UUID tapId, String newSecret) {
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
        // We are extremely defensive with exception catching here to make sure it always executes the deletion query.

        List<UUID> sessionsToClean = Lists.newArrayList();

        // Apply global authentication settings to all org admins and super admins.
        try {
            int sessionTimeoutMinutes = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                    .getValue(AuthenticationRegistryKeys.SESSION_TIMEOUT_MINUTES.key())
                    .orElse(AuthenticationRegistryKeys.SESSION_TIMEOUT_MINUTES.defaultValue().get()));

            int sessionInactivityTimeoutMinutes = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                    .getValue(AuthenticationRegistryKeys.SESSION_INACTIVITY_TIMEOUT_MINUTES.key())
                    .orElse(AuthenticationRegistryKeys.SESSION_INACTIVITY_TIMEOUT_MINUTES.defaultValue().get()));

            int mfaTimeoutMinutes = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                    .getValue(AuthenticationRegistryKeys.MFA_TIMEOUT_MINUTES.key())
                    .orElse(AuthenticationRegistryKeys.MFA_TIMEOUT_MINUTES.defaultValue().get()));

            // Find all users with sessions older than 12 hours and sessions with MFA waiting for longer than 5 minutes.
            sessionsToClean.addAll(
                    nzyme.getDatabase().withHandle(handle ->
                            handle.createQuery("SELECT u.uuid FROM auth_users AS u " +
                                            "LEFT JOIN auth_sessions AS s ON s.user_id = u.uuid " +
                                            "WHERE (u.is_orgadmin = true OR u.is_superadmin = true) " +
                                            "AND (s.created_at < :session_timeout OR (s.mfa_valid = true " +
                                            "AND u.last_activity < :inactivity_timeout) OR (s.mfa_valid = false " +
                                            "AND s.mfa_requested_at < :mfa_timeout))")
                                    .bind("session_timeout", DateTime.now().minusMinutes(sessionTimeoutMinutes))
                                    .bind("inactivity_timeout", DateTime.now().minusMinutes(sessionInactivityTimeoutMinutes))
                                    .bind("mfa_timeout", DateTime.now().minusMinutes(mfaTimeoutMinutes))
                                    .mapTo(UUID.class)
                                    .list()
                    )
            );
        } catch(Exception e) {
            LOG.error("Could not determine sessions of super admins and org admins to clean.", e);
        }

        // Apply tenant-specific authentication settings to all tenant users.
        try {
            for (OrganizationEntry organization : findAllOrganizations(Integer.MAX_VALUE, 0)) {
                Optional<List<TenantEntry>> tenants = findAllTenantsOfOrganization(organization.uuid());
                if (tenants.isPresent()) {
                    for (TenantEntry tenant : tenants.get()) {
                        sessionsToClean.addAll(
                                nzyme.getDatabase().withHandle(handle ->
                                        handle.createQuery("SELECT u.uuid FROM auth_users AS u " +
                                                        "LEFT JOIN auth_sessions AS s ON s.user_id = u.uuid " +
                                                        "WHERE u.organization_id = :organization_id " +
                                                        "AND u.tenant_id = :tenant_id " +
                                                        "AND (s.created_at < :session_timeout OR (s.mfa_valid = true " +
                                                        "AND u.last_activity < :inactivity_timeout) " +
                                                        "OR (s.mfa_valid = false AND s.mfa_requested_at < :mfa_timeout))")
                                                .bind("organization_id", organization.uuid())
                                                .bind("tenant_id", tenant.uuid())
                                                .bind("session_timeout", DateTime.now().minusMinutes(tenant.sessionTimeoutMinutes()))
                                                .bind("inactivity_timeout", DateTime.now().minusMinutes(tenant.sessionInactivityTimeoutMinutes()))
                                                .bind("mfa_timeout", DateTime.now().minusMinutes(tenant.mfaTimeoutMinutes()))
                                                .mapTo(UUID.class)
                                                .list()
                                )
                        );
                    }
                }
            }

        } catch(Exception e) {
            LOG.error("Could not determine sessions of tenant users to clean.", e);
        }

        if (!sessionsToClean.isEmpty()) {
            try {
                nzyme.getDatabase().useHandle(handle ->
                        handle.createUpdate("DELETE FROM auth_sessions WHERE user_id IN (<user_ids>)")
                                .bindList("user_ids", sessionsToClean)
                                .execute()
                );
            } catch(Exception e) {
                LOG.error("Could not delete sessions marked for deletion.", e);
            }
        }
    }

    public boolean isTenantDeletable(TenantEntry t) {
        return countTapsOfTenant(t) == 0 && countUsersOfTenant(t) == 0;
    }

}
