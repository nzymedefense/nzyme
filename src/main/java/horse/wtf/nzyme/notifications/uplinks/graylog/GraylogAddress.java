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

package horse.wtf.nzyme.notifications.uplinks.graylog;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GraylogAddress {

    public abstract String host();
    public abstract int port();

    public static GraylogAddress create(String host, int port) {
        return builder()
                .host(host)
                .port(port)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GraylogAddress.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder host(String host);

        public abstract Builder port(int port);

        public abstract GraylogAddress build();
    }

}
