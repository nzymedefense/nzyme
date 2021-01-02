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

import com.google.auto.value.AutoValue;
import com.typesafe.config.Config;
import horse.wtf.nzyme.dot11.deception.traps.Trap;

@AutoValue
public abstract class Dot11TrapConfiguration {

    public abstract Trap.Type type();
    public abstract Config configuration();

    public static Dot11TrapConfiguration create(Trap.Type type, Config configuration) {
        return builder()
                .type(type)
                .configuration(configuration)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11TrapConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(Trap.Type type);

        public abstract Builder configuration(Config configuration);

        public abstract Dot11TrapConfiguration build();
    }

}