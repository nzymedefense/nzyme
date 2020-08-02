package horse.wtf.nzyme.configuration;

import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.ResourcesAccessingTest;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.configuration.leader.LeaderConfigurationLoader;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;

import static org.testng.Assert.*;

public class NzymeLeaderConfigurationLoaderTest extends ResourcesAccessingTest {

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testGetConfigWithNonExistentFile() throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        new LeaderConfigurationLoader(new File("idontexist.conf"), false).get();
    }

    @Test
    public void testGetValidConfig() throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        LeaderConfiguration c = new LeaderConfigurationLoader(loadFromResourceFile("nzyme-test-complete-valid.conf"), false).get();

        assertEquals(c.role(), Role.LEADER);
        assertFalse(c.databasePath().isEmpty()); // This one is different based on ENV vars
        assertEquals(c.pythonExecutable(), "/usr/bin/python2.7");
        assertEquals(c.pythonScriptDirectory(), "/tmp");
        assertEquals(c.pythonScriptPrefix(), "nzyme_");
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
                add(Dot11BSSIDDefinition.create("06:0d:2d:c9:36:23", ImmutableList.of("c9ed4adc12dc3e17208446b6a10070b70a73b9ce3a99215e05426faea6de91c7")));
                add(Dot11BSSIDDefinition.create("24:a4:3c:7d:01:cc", ImmutableList.of("def456")));
            }}, new ArrayList<Integer>() {{
                add(1);
                add(3);
                add(6);
                add(11);
            }}, new ArrayList<String>() {{
                add("NONE");
            }}, 40));

            add(Dot11NetworkDefinition.create("WTF", new ArrayList<Dot11BSSIDDefinition>() {{
                add(Dot11BSSIDDefinition.create("00:c0:ca:95:68:3b",  ImmutableList.of("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b")));
            }}, new ArrayList<Integer>() {{
                add(1);
                add(11);
            }}, new ArrayList<String>() {{
                add("WPA1-EAM-PSK-CCMP");
                add("WPA2-EAM-PSK-CCMP");
            }}, 40));
        }});
        assertEquals(c.dot11Alerts(), new ArrayList<Alert.TYPE_WIDE>() {{
            add(Alert.TYPE_WIDE.UNEXPECTED_BSSID);
            add(Alert.TYPE_WIDE.UNEXPECTED_SSID);
            add(Alert.TYPE_WIDE.UNEXPECTED_CHANNEL);
            add(Alert.TYPE_WIDE.CRYPTO_CHANGE);
        }});
    }

    @Test(expectedExceptions = IncompleteConfigurationException.class)
    public void testGetInvalidConfigIncomplete() throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        new LeaderConfigurationLoader(loadFromResourceFile("nzyme-test-incomplete.conf"), false).get();
    }

}