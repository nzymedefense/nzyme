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

package horse.wtf.nzyme.remote.forwarders;

import com.typesafe.config.Config;
import horse.wtf.nzyme.configuration.ConfigurationKeys;
import horse.wtf.nzyme.configuration.ForwarderDefinition;

import java.net.InetSocketAddress;

public class ForwarderFactory {

    private final String nzymeId;

    public ForwarderFactory(String nzymeId) {
        this.nzymeId = nzymeId;
    }

    public Forwarder fromConfigurationDefinition(ForwarderDefinition definition) {
        String def = definition.type().toLowerCase();
        switch(def) {
            case "udp":
                return new UDPForwarder(parseInetSocketAddress(definition.configuration()), nzymeId);
            default:
                throw new RuntimeException("Unknown forwarder type [" + def + "].");
        }
    }

    private InetSocketAddress parseInetSocketAddress(Config config) {
        if(config.hasPath(ConfigurationKeys.HOST) && config.hasPath(ConfigurationKeys.PORT)) {
            return new InetSocketAddress(config.getString(ConfigurationKeys.HOST), config.getInt(ConfigurationKeys.PORT));
        } else {
            throw new RuntimeException("Invalid configuration. Expecting \"host\" and \"port\" set in forwarder configuration. Please consult the forwarder documentation.");
        }
    }

}
