package app.nzyme.core.rest.authentication;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.security.sessions.Session;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class RESTAuthenticationFilterTest {

    @Test
    public void testFilterLetsValidTokenPass() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + Session.createToken("admin", nzyme.getSigningKey())
        );

        f.filter(ctx);
        assertFalse(ctx.aborted);
    }

    @Test
    public void testFilterRejectsInvalidSigningKey() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + Session.createToken("admin", Keys.secretKeyFor(SignatureAlgorithm.HS512))
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsEmptyToken() throws IOException {
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

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Wtf " + Session.createToken("admin", nzyme.getSigningKey())
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

}