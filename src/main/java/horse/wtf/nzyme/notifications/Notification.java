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

package horse.wtf.nzyme.notifications;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;

import java.util.Map;

public class Notification {

    private final ImmutableMap.Builder<String, Object> fields;
    private final String message;
    private final Dot11Probe probe;

    public Notification(String message, int channel, Dot11Probe probe) {
        this.fields = new ImmutableMap.Builder<>();
        this.message = message;
        this.probe = probe;

        addField("channel", channel);
    }

    public Notification addField(String key, Object value) {
        if(value == null) {
            return this;
        }

        fields.put("_" + key, value);

        return this;
    }

    public Notification addFields(Map<String, Object> x) {
        if (x == null) {
            return this;
        }

        fields.putAll(x);

        return this;
    }

    public Map<String, Object> getAdditionalFields() {
        return fields.build();
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("802.11 frame: ").append(message)
                .append(" - Details: ")
                .append(Joiner.on(", ").join(getAdditionalFields().entrySet()));

        return sb.toString();
    }

    public Dot11Probe getProbe() {
        return probe;
    }

}
