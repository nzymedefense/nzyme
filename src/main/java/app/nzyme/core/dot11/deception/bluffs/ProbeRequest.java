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

package app.nzyme.core.dot11.deception.bluffs;

import com.google.common.collect.ImmutableMap;
import app.nzyme.core.configuration.leader.LeaderConfiguration;

import java.util.Map;

public class ProbeRequest extends Bluff {

    private final String interfaceName;
    private final String ssid;
    private final String mac;

    public ProbeRequest(LeaderConfiguration configuration, String interfaceName, String ssid, String mac) {
        super(configuration);

        this.interfaceName = interfaceName;
        this.ssid = ssid;
        this.mac = mac;
    }

    @Override
    protected String scriptCategory() {
        return "dot11";
    }

    @Override
    protected String scriptName() {
        return "probe_request.py";
    }

    @Override
    protected Map<String, String> parameters() {
        ImmutableMap.Builder<String, String> params = new ImmutableMap.Builder<>();

        params.put("--interface", interfaceName);
        params.put("--ssid", ssid);
        params.put("--mac", mac);

        return params.build();
    }

}
