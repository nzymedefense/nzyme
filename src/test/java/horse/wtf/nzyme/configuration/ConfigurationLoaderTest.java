package horse.wtf.nzyme.configuration;

import horse.wtf.nzyme.Role;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;

import static org.testng.Assert.*;

public class ConfigurationLoaderTest {

    private File loadFromFile(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new RuntimeException("test config file does not exist in resources");
        }

        return new File(resource.getFile());
    }

    @Test
    public void testGetValidConfig() throws ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException {
        Configuration c = new ConfigurationLoader(loadFromFile("nzyme-test-complete-valid.conf"), false).get();

        assertTrue(c.versionchecksEnabled());
        assertTrue(c.fetchOuis());
        assertEquals(c.role(), Role.LEADER);
        assertEquals(c.nzymeId(), "nzyme-testng");
        assertEquals(c.databasePath(), "nzyme.db");
        assertEquals(c.pythonExecutable(), "/usr/bin/python2.7");
        assertEquals(c.pythonScriptDirectory(), "/tmp");
        assertEquals(c.pythonScriptPrefix(), "nzyme_");
        assertEquals(c.alertingRetentionPeriodMinutes(), 15);
        assertEquals(c.alertingTrainingPeriodSeconds(), 300);
    }

}