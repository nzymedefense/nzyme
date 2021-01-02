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

package horse.wtf.nzyme.notifications;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;

import java.util.Map;

public class Notification {

    private final ImmutableMap.Builder<String, Object> fields;
    private final String message;

    public Notification(String message, int channel) {
        this.fields = new ImmutableMap.Builder<>();
        this.message = message;

        addField("channel", channel);
    }

    public Notification addField(String key, Object value) {
        if(value == null) {
            return this;
        }

        fields.put(key, value);

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

}
