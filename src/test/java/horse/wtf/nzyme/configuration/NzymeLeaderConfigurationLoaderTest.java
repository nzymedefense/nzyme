/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.configuration;

import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.ResourcesAccessingTest;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.configuration.leader.LeaderConfigurationLoader;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.testng.annotations.Test;

import javax.mail.Message;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
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
        String configFile = "nzyme-test-complete-valid.conf.test";
        if (System.getProperty("os.name").startsWith("Windows")) {
            configFile = "nzyme-test-complete-valid-windows.conf.test";
            System.out.println("loading Windows nzyme configuration file");
        }

        LeaderConfiguration c = new LeaderConfigurationLoader(loadFromResourceFile(configFile), false).get();

        assertEquals(c.role(), Role.LEADER);
        assertFalse(c.databasePath().isEmpty()); // This one is different based on ENV vars
        assertEquals(c.pythonScriptPrefix(), "nzyme_");
        assertEquals(c.alertingTrainingPeriodSeconds(), 300);
        assertTrue(c.fetchOuis());
        assertTrue(c.versionchecksEnabled());
        assertEquals(c.restListenUri(), URI.create("http://127.0.0.1:22900/"));
        assertEquals(c.dot11Monitors(), new ArrayList<Dot11MonitorDefinition>() {{
            add(Dot11MonitorDefinition.create("wlx00c0ca8fd89a", ImmutableList.of(1,2,3,4,5,6), "sudo /sbin/iwconfig {interface} channel {channel}", 1, false));
            add(Dot11MonitorDefinition.create("wlx00c0ca971216", ImmutableList.of(7,8,9,10,11), "sudo /sbin/iwconfig {interface} channel {channel}", 3, false));
        }});
        assertEquals(c.dot11Networks(), new ArrayList<Dot11NetworkDefinition>() {{
            add(Dot11NetworkDefinition.create("United_Wi-Fi", new ArrayList<Dot11BSSIDDefinition>() {{
                add(Dot11BSSIDDefinition.create("06:0d:2d:c9:36:23", ImmutableList.of("c9ed4adc12dc3e17208446b6a10070b70a73b9ce3a99215e05426faea6de91c7"), null));
                add(Dot11BSSIDDefinition.create("24:a4:3c:7d:01:cc", ImmutableList.of("def456"), null));
            }}, new ArrayList<Integer>() {{
                add(1);
                add(3);
                add(6);
                add(11);
            }}, new ArrayList<String>() {{
                add("NONE");
            }}, 40));

            add(Dot11NetworkDefinition.create("WTF", new ArrayList<Dot11BSSIDDefinition>() {{
                add(Dot11BSSIDDefinition.create("00:c0:ca:95:68:3b",  ImmutableList.of("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b"), null));
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

        assertEquals(c.uplinks().size(), 4);
        assertEquals(c.uplinks().get(0).type(), "syslog_udp_rfc5424");
        assertEquals(c.uplinks().get(0).configuration().getString(ConfigurationKeys.HOST), "localhost");
        assertEquals(c.uplinks().get(0).configuration().getInt(ConfigurationKeys.PORT), 5516);

        assertEquals(c.uplinks().get(1).type(), "graylog");
        assertEquals(c.uplinks().get(1).configuration().getString(ConfigurationKeys.HOST), "example.org");
        assertEquals(c.uplinks().get(1).configuration().getInt(ConfigurationKeys.PORT), 5517);

        assertEquals(c.uplinks().get(2).type(), "graylog");
        assertEquals(c.uplinks().get(2).configuration().getString(ConfigurationKeys.HOST), "10.243.255.10");
        assertEquals(c.uplinks().get(2).configuration().getInt(ConfigurationKeys.PORT), 33001);

        assertEquals(c.uplinks().get(3).type(), "graylog");
        assertEquals(c.uplinks().get(3).configuration().getString(ConfigurationKeys.HOST), "127.0.0.1");
        assertEquals(c.uplinks().get(3).configuration().getInt(ConfigurationKeys.PORT), 9001);

        assertEquals(c.remoteInputAddress(), new InetSocketAddress("0.0.0.0", 9002));

        assertEquals(c.reporting().email().transportStrategy(), TransportStrategy.SMTP_TLS);
        assertEquals(c.reporting().email().host(), "smtp.example.org");
        assertEquals(c.reporting().email().port(), 587);
        assertEquals(c.reporting().email().username(), "your_username");
        assertEquals(c.reporting().email().password(), "your_password");
        assertEquals(c.reporting().email().from(), new Recipient("nzyme", "nzyme@example.org", Message.RecipientType.TO));
        assertEquals(c.reporting().email().subjectPrefix(), "[NZYME]");
    }

    @Test(expectedExceptions = IncompleteConfigurationException.class)
    public void testGetInvalidConfigIncomplete() throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        new LeaderConfigurationLoader(loadFromResourceFile("nzyme-test-incomplete.conf.test"), false).get();
    }

}