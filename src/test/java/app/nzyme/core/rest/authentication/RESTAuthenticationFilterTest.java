package app.nzyme.core.rest.authentication;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class RESTAuthenticationFilterTest {

    @Test
    public void testFilterLetsValidTokenPass() throws IOException {
        fail();
        /*NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + SessionId.createToken("admin", nzyme.getSigningKey())
        );

        f.filter(ctx);
        assertFalse(ctx.aborted);*/
    }

    @Test
    public void testFilterRejectsInvalidSigningKey() throws IOException {
        fail();

        /*NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + SessionId.createToken("admin", Keys.secretKeyFor(SignatureAlgorithm.HS512))
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);*/
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
        fail();

        /*NzymeNode nzyme = new MockNzyme();
        RESTAuthenticationFilter f = new RESTAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Wtf " + SessionId.createToken("admin", nzyme.getSigningKey())
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);*/
    }

}