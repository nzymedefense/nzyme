package app.nzyme.core.rest.authentication;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.core.security.sessions.SessionId;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class RESTAuthenticationFilterTest {

    @BeforeMethod
    public void clean() {
        MockNzyme nzyme = new MockNzyme();

        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_sessions;").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_users;").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_tenants;").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_organizations;").execute());

    }

    private UserEntry createUser(String email, String password) {
        MockNzyme nzyme = new MockNzyme();

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());
        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(password);

        OrganizationEntry org = nzyme.getAuthenticationService().createOrganization("test org", "test org");
        TenantEntry tenant = nzyme.getAuthenticationService().createTenant(org.id(), "test tenant", "test tenant");

        return nzyme.getAuthenticationService().createUserOfTenant(org.id(), tenant.id(), "test user", email, hash);
    }

    private String createSession(long userId) {
        MockNzyme nzyme = new MockNzyme();

        String sessionId = SessionId.createSessionId();

        nzyme.getAuthenticationService().createSession(sessionId, userId, "127.0.0.1");

        return sessionId;
    }

    @Test
    public void testFilterLetsValidSessionPass() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        createUser("test@example.org", "123123123123");
        UserEntry user = createUser("lennart@example.org", "456456456456");
        String sessionId = createSession(user.id());

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + sessionId
        );

        f.filter(ctx);
        assertFalse(ctx.aborted);
    }

    @Test
    public void testFilterRejectsNotExistingSession() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        createUser("test@example.org", "123123123123");
        createUser("lennart@example.org", "456456456456");

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + SessionId.createSessionId()
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsEmptySessionId() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest("Bearer ");

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsEmptyAuthHeader() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest("");

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsUnknownAuthScheme() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        createUser("test@example.org", "123123123123");
        UserEntry user = createUser("lennart@example.org", "456456456456");
        String sessionId = createSession(user.id());

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Wtf " + sessionId
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

}