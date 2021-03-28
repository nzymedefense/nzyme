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

package horse.wtf.nzyme.notifications.uplinks;

import com.typesafe.config.Config;
import horse.wtf.nzyme.configuration.ConfigurationKeys;
import horse.wtf.nzyme.configuration.UplinkDefinition;
import horse.wtf.nzyme.notifications.Uplink;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogUplink;
import horse.wtf.nzyme.notifications.uplinks.syslog.SyslogUDPRFC3164UDPUplink;
import horse.wtf.nzyme.notifications.uplinks.syslog.SyslogUDPRFC5424UDPUplink;

import java.net.InetSocketAddress;

public class UplinkFactory {

    private final String nzymeId;

    public UplinkFactory(String nzymeId) {
        this.nzymeId = nzymeId;
    }

    public Uplink fromConfigurationDefinition(UplinkDefinition definition) {
        String def = definition.type().toLowerCase();
        switch(def) {
            case "graylog":
                return new GraylogUplink(parseInetSocketAddress(definition.configuration()), nzymeId);
            case "syslog_udp_rfc5424":
                return new SyslogUDPRFC5424UDPUplink(parseInetSocketAddress(definition.configuration()), nzymeId);
            case "syslog_udp_rfc3164":
                return new SyslogUDPRFC3164UDPUplink(parseInetSocketAddress(definition.configuration()), nzymeId);
            default:
                throw new RuntimeException("Unknown uplink type [" + def + "].");
        }
    }

    private InetSocketAddress parseInetSocketAddress(Config config) {
        if(config.hasPath(ConfigurationKeys.HOST) && config.hasPath(ConfigurationKeys.PORT)) {
            return InetSocketAddress.createUnresolved(config.getString(ConfigurationKeys.HOST), config.getInt(ConfigurationKeys.PORT));
        } else {
            throw new RuntimeException("Invalid configuration. Expecting \"host\" and \"port\" set in uplink configuration. Please consult the uplink documentation.");
        }
    }

}
