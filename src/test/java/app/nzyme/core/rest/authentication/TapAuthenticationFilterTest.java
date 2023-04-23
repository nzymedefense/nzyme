package app.nzyme.core.rest.authentication;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
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
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_tenants").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("DELETE FROM auth_organizations").execute());
    }

    private TapPermissionEntry buildTap() {
        NzymeNode nzyme = new MockNzyme();

        OrganizationEntry org = nzyme.getAuthenticationService()
                .createOrganization("test org", "test org");

        TenantEntry tenant = nzyme.getAuthenticationService()
                .createTenant(org.id(), "test tenant", "test tenant");

        String secret = RandomStringUtils.random(64, true, true);

        return nzyme.getAuthenticationService()
                .createTap(org.id(), tenant.id(), secret, "test tap", "test tap");
    }

    @Test
    public void testFilterLetsValidSecretPass() throws IOException {
        buildTap();
        TapPermissionEntry tap = buildTap();

        NzymeNode nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Bearer " + tap.secret()
        );

        f.filter(ctx);
        assertFalse(ctx.aborted);
    }

    @Test()
    public void testFilterRejectsInvalidSecret() throws IOException {
        buildTap();
        buildTap();

        NzymeNode nzyme = new MockNzyme();
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
        buildTap();
        TapPermissionEntry tap = buildTap();

        NzymeNode nzyme = new MockNzyme();
        TapAuthenticationFilter f = new TapAuthenticationFilter(nzyme);

        MockHeaderContainerRequest ctx = new MockHeaderContainerRequest(
                "Wtf " + tap.secret()
        );

        f.filter(ctx);
        assertTrue(ctx.aborted);
    }

}