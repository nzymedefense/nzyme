package app.nzyme.core.rest.authentication;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.security.authentication.AuthenticationService;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class TapAuthenticationFilterTest {

    @BeforeMethod
    public void clean() {
        NzymeNode nzyme = new MockNzyme();

        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM bus_channels").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM tap_buses").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM taps").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_users").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_tenants").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_organizations").execute());
    }

    private String buildTap(NzymeNode nzyme) {
        OrganizationEntry org = nzyme.getAuthenticationService()
                .createOrganization("test org", "test org");

        TenantEntry tenant = nzyme.getAuthenticationService()
                .createTenant(org.uuid(),
                        "test tenant",
                        "test tenant",
                        720,
                        15,
                        5);

        String secret = RandomStringUtils.random(64, true, true);

       nzyme.getAuthenticationService()
                .createTap(org.uuid(), tenant.uuid(), secret, "test tap", "test tap");

       return secret;
    }

    @Test
    public void testFilterLetsValidSecretPass() throws IOException {
        NzymeNode nzyme = new MockNzyme();

        buildTap(nzyme);
        String secret = buildTap(nzyme);

        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + secret
        );

        f.filter(ctx);
        assertFalse(ctx.aborted);
    }

    @Test()
    public void testFilterRejectsInvalidSecret() throws IOException {
        NzymeNode nzyme = new MockNzyme();

        buildTap(nzyme);
        buildTap(nzyme);

        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + RandomStringUtils.random(64, true, true)
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsEmptySecret() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest("Bearer ");

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsEmptyAuthHeader() throws IOException {
        NzymeNode nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest("");

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

    @Test
    public void testFilterRejectsUnknownAuthScheme() throws IOException {
        NzymeNode nzyme = new MockNzyme();

        buildTap(nzyme);
        String secret = buildTap(nzyme);

        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Wtf " + secret
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

}