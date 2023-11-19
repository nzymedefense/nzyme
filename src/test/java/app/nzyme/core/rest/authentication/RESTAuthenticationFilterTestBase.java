package app.nzyme.core.rest.authentication;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.security.authentication.AuthenticationService;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.core.security.sessions.SessionId;
import org.testng.annotations.BeforeMethod;

import java.util.UUID;

public class RESTAuthenticationFilterTestBase {

    @BeforeMethod
    public void clean() {
        MockNzyme nzyme = new MockNzyme();

        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_sessions;").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_users;").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_tenants;").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_organizations;").execute());

    }

    protected UserEntry createUser(String email, String password) {
        MockNzyme nzyme = new MockNzyme();

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());
        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(password);

        OrganizationEntry org = nzyme.getAuthenticationService().createOrganization("test org", "test org");
        TenantEntry tenant = nzyme.getAuthenticationService().createTenant(
                org.uuid(),
                "test tenant",
                "test tenant",
                720,
                15,
                5
        );

        UserEntry user = nzyme.getAuthenticationService().createUserOfTenant(org.uuid(), tenant.uuid(), "test user", email, hash);

        nzyme.getAuthenticationService().setUserMFAComplete(user.uuid(), true);

        return user;
    }

    protected String createSession(UUID userId, boolean passedMfa) {
        MockNzyme nzyme = new MockNzyme();

        String sessionId = SessionId.createSessionId();

        nzyme.getAuthenticationService().createSession(sessionId, userId, "127.0.0.1");

        if (passedMfa) {
            nzyme.getAuthenticationService().markSessionAsMFAValid(sessionId);
        }

        return sessionId;
    }

}
