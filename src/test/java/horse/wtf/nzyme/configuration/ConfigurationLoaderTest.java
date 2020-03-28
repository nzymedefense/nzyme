package horse.wtf.nzyme.configuration;

import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.ResourcesAccessingTest;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.Assert.*;

public class ConfigurationLoaderTest extends ResourcesAccessingTest {

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testGetConfigWithNonExistentFile() throws ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException, FileNotFoundException {
        new ConfigurationLoader(new File("idontexist.conf"), false).get();
    }

    @Test
    public void testGetValidConfig() throws ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException, FileNotFoundException {
        Configuration c = new ConfigurationLoader(loadFromResourceFile("nzyme-test-complete-valid.conf"), false).get();

        assertEquals(c.role(), Role.LEADER);
        assertEquals(c.nzymeId(), "nzyme-testng");
        assertFalse(c.databasePath().isEmpty()); // This one is different based on ENV vars
        assertEquals(c.pythonExecutable(), "/usr/bin/python2.7");
        assertEquals(c.pythonScriptDirectory(), "/tmp");
        assertEquals(c.pythonScriptPrefix(), "nzyme_");
        assertEquals(c.alertingRetentionPeriodMinutes(), 15);
        assertEquals(c.alertingTrainingPeriodSeconds(), 300);
        assertTrue(c.fetchOuis());
        assertTrue(c.versionchecksEnabled());
        assertEquals(c.restListenUri(), URI.create("http://127.0.0.1:22900/"));
        assertEquals(c.graylogUplinks(), new ArrayList<GraylogAddress>() {{
            add(GraylogAddress.create("10.243.255.10", 33001));
            add(GraylogAddress.create("127.0.0.1", 9001));
        }});
        assertEquals(c.dot11Monitors(), new ArrayList<Dot11MonitorDefinition>() {{
            add(Dot11MonitorDefinition.create("wlx00c0ca8fd89a", new ArrayList<Integer>() {{
                add(1);
                add(2);
                add(3);
                add(4);
                add(5);
                add(6);
            }}, "sudo /sbin/iwconfig {interface} channel {channel}", 1));
            add(Dot11MonitorDefinition.create("wlx00c0ca971216", new ArrayList<Integer>() {{
                add(7);
                add(8);
                add(9);
                add(10);
                add(11);
            }}, "sudo /sbin/iwconfig {interface} channel {channel}", 3));
        }});
        assertEquals(c.dot11Networks(), new ArrayList<Dot11NetworkDefinition>() {{
            add(Dot11NetworkDefinition.create("United_Wi-Fi", new ArrayList<Dot11BSSIDDefinition>() {{
                add(Dot11BSSIDDefinition.create("06:0d:2d:c9:36:23", ImmutableList.of("abc123")));
                add(Dot11BSSIDDefinition.create("24:a4:3c:7d:01:cc", ImmutableList.of("def456")));
            }}, new ArrayList<Integer>() {{
                add(1);
                add(6);
                add(11);
            }}, new ArrayList<String>() {{
                add("None");
            }}, 40));

            add(Dot11NetworkDefinition.create("WTF", new ArrayList<Dot11BSSIDDefinition>() {{
                add(Dot11BSSIDDefinition.create("00:c0:ca:95:68:3b",  ImmutableList.of("123456")));
            }}, new ArrayList<Integer>() {{
                add(1);
                add(2);
                add(3);
                add(4);
                add(5);
                add(6);
                add(7);
                add(8);
                add(9);
                add(10);
                add(11);
                add(12);
                add(13);
            }}, new ArrayList<String>() {{
                add("WPA1-EAM-PSK-CCMP-TKIP");
                add("WPA2-EAM-PSK-CCMP-TKIP");
            }}, 40));
        }});
        assertEquals(c.dot11Alerts(), new ArrayList<Alert.TYPE_WIDE>() {{
            add(Alert.TYPE_WIDE.UNEXPECTED_BSSID);
            add(Alert.TYPE_WIDE.UNEXPECTED_SSID);
            add(Alert.TYPE_WIDE.UNEXPECTED_CHANNEL);
            add(Alert.TYPE_WIDE.CRYPTO_CHANGE);
        }});
    }

    @Test(expectedExceptions = ConfigurationLoader.IncompleteConfigurationException.class)
    public void testGetInvalidConfigIncomplete() throws ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException, FileNotFoundException {
        new ConfigurationLoader(loadFromResourceFile("nzyme-test-incomplete.conf"), false).get();
    }

}