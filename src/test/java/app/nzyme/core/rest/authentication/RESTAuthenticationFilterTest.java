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

public class RESTAuthenticationFilterTest extends RESTAuthenticationFilterTestBase {

    @Test
    public void testFilterLetsValidSessionPass() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        createUser("test@example.org", "123123123123");
        UserEntry user = createUser("lennart@example.org", "456456456456");
        String sessionId = createSession(user.uuid(), true);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + sessionId
        );

        f.filter(ctx);
        assertFalse(ctx.aborted);
    }

    @Test
    public void testFilterRejectsSessionWithoutPassedMFA() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        createUser("test@example.org", "123123123123");
        UserEntry user = createUser("lennart@example.org", "456456456456");
        String sessionId = createSession(user.uuid(), false);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + sessionId
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
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
        String sessionId = createSession(user.uuid(), true);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Wtf " + sessionId
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

}