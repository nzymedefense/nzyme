package app.nzyme.core.rest.authentication;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeLeader;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class TapAuthenticationFilterTest {

    @Test
    public void testFilterLetsValidSecretPass() throws IOException {
        NzymeLeader nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + nzyme.getConfigurationService().getConfiguration().tapSecret()
        );

        f.filter(ctx);
        assertFalse(ctx.aborted);
    }

    @Test()
    public void testFilterRejectsInvalidSecret() throws IOException {
        NzymeLeader nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + RandomStringUtils.random(64, true, true)
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsEmptySecret() throws IOException {
        NzymeLeader nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest("Bearer ");

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsEmptyAuthHeader() throws IOException {
        NzymeLeader nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest("");

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsUnknownAuthScheme() throws IOException {
        NzymeLeader nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Wtf " + nzyme.getConfigurationService().getConfiguration().tapSecret()
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

}