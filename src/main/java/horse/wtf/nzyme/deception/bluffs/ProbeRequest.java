/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.deception.bluffs;

import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.configuration.Configuration;

import java.util.Map;

public class ProbeRequest extends Bluff {

    private final String interfaceName;
    private final String ssid;
    private final String mac;

    public ProbeRequest(Configuration configuration, String interfaceName, String ssid, String mac) {
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
