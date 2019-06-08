package horse.wtf.nzyme.configuration;

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

public class ConfigurationLoaderTest {

    private File loadFromResourceFile(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new RuntimeException("test config file does not exist in resources");
        }

        return new File(resource.getFile());
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testGetConfigWithNonExistentFile() throws ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException, FileNotFoundException {
        new ConfigurationLoader(new File("idontexist.conf"), false).get();
    }

    @Test
    public void testGetValidConfig() throws ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException, FileNotFoundException {
        Configuration c = new ConfigurationLoader(loadFromResourceFile("nzyme-test-complete-valid.conf"), false).get();

        assertEquals(c.role(), Role.LEADER);
        assertEquals(c.nzymeId(), "nzyme-testng");
        assertEquals(c.databasePath(), "nzyme.db");
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
            add(Dot11NetworkDefinition.create("United_Wi-Fi", new ArrayList<String>() {{
                add("06:0d:2d:c9:36:23");
                add("24:a4:3c:7d:01:cc");
            }}, new ArrayList<Integer>() {{
                add(1);
                add(6);
                add(11);
            }}, new ArrayList<String>() {{
                add("None");
            }}));

            add(Dot11NetworkDefinition.create("WTF", new ArrayList<String>() {{
                add("00:c0:ca:95:68:3b");
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
            }}));
        }});
        assertEquals(c.dot11Alerts(), new ArrayList<Alert.TYPE_WIDE>() {{
            add(Alert.TYPE_WIDE.UNEXPECTED_BSSID);
            add(Alert.TYPE_WIDE.UNEXPECTED_SSID);
            add(Alert.TYPE_WIDE.UNEXPECTED_CHANNEL);
            add(Alert.TYPE_WIDE.CRYPTO_CHANGE);
            add(Alert.TYPE_WIDE.KNOWN_BANDIT_FINGERPRINT);
        }});
        assertEquals(c.knownBanditFingerprints(), new HashMap<String, BanditFingerprintDefinition>() {{
            put("ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c", BanditFingerprintDefinition.create(
                    "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                    "WiFi Pineapple Nano or Tetra (PineAP)"
            ));
            put("e1a3923e4a513e2e1040763ad0b97746a84add27d559a84e4af3b313c69bfb26", BanditFingerprintDefinition.create(
                    "e1a3923e4a513e2e1040763ad0b97746a84add27d559a84e4af3b313c69bfb26",
                    "WiFi Pineapple Nano (management access point)"
            ));
        }});
    }

    @Test(expectedExceptions = ConfigurationLoader.IncompleteConfigurationException.class)
    public void testGetInvalidConfigIncomplete() throws ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException, FileNotFoundException {
        new ConfigurationLoader(loadFromResourceFile("nzyme-test-incomplete.conf"), false).get();
    }

}