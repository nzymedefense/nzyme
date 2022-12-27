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

package app.nzyme.core.dot11.networks.sentry.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SentrySSID {

    @Nullable
    public abstract String ssid();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static SentrySSID create(String ssid, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .ssid(ssid)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SentrySSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract SentrySSID build();
    }

}
