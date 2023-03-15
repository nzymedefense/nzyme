package app.nzyme.core.configuration.db;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BaseConfigurationServiceTest {

    @BeforeTest
    public void resetConfig() {
        NzymeNode nzyme = new MockNzyme(10);
        nzyme.getDatabase().useHandle(handle -> handle.execute("TRUNCATE base_configuration"));
    }

    @Test
    public void testInitialize() {
        BaseConfigurationService s = new BaseConfigurationService(new MockNzyme());
        s.initialize();
    }

    @Test
    public void testGetInitialRow() {
        BaseConfigurationService s = new BaseConfigurationService(new MockNzyme());
        s.initialize();
        BaseConfiguration c = s.getConfiguration();

        assertTrue(c.tapSecret().length() == 64);
        assertTrue(c.updatedAt().withZone(DateTimeZone.UTC).isAfter(DateTime.now().withZone(DateTimeZone.UTC).minusSeconds(300))
                && c.updatedAt().withZone(DateTimeZone.UTC).isBefore(DateTime.now().withZone(DateTimeZone.UTC).plusSeconds(300)));

        resetConfig();

        BaseConfigurationService s2 = new BaseConfigurationService(new MockNzyme());
        s2.initialize();
        BaseConfiguration c2 = s2.getConfiguration();

        assertNotEquals(c.tapSecret(), c2.tapSecret());
    }

    @Test
    public void testSetTapSecret() {
        BaseConfigurationService s = new BaseConfigurationService(new MockNzyme());
        s.initialize();

        String newSecret = RandomStringUtils.random(64, true, true);

        assertNotEquals(s.getConfiguration().tapSecret(), newSecret);

        s.setTapSecret(newSecret);

        assertEquals(s.getConfiguration().tapSecret(), newSecret);
    }

}