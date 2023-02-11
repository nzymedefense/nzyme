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

package app.nzyme.core.notifications.uplinks;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import app.nzyme.core.configuration.UplinkDefinition;
import app.nzyme.core.notifications.Uplink;
import org.testng.annotations.Test;

public class UplinkFactoryTest {

    @Test
    public void testGraylogUplink() {
        UplinkFactory f = new UplinkFactory("foo");

        Config config = ConfigFactory.empty()
                .withValue("host", ConfigValueFactory.fromAnyRef("example.com"))
                .withValue("port", ConfigValueFactory.fromAnyRef(1234));
        Uplink u = f.fromConfigurationDefinition(UplinkDefinition.create("graylog", config));
    }

    @Test
    public void testGyslogUDPRFC5424Uplink() {
        UplinkFactory f = new UplinkFactory("foo");

        Config config = ConfigFactory.empty()
                .withValue("host", ConfigValueFactory.fromAnyRef("localhost"))
                .withValue("port", ConfigValueFactory.fromAnyRef(1234));
        Uplink u = f.fromConfigurationDefinition(UplinkDefinition.create("syslog_udp_rfc5424", config));
    }

}