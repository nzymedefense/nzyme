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

package horse.wtf.nzyme.bandits.engine;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ContactRecorderSignalStrengthHistogramEntry {

    public abstract long signalStrength();
    public abstract DateTime createdAt();

    public static ContactRecorderSignalStrengthHistogramEntry create(long signalStrength, DateTime createdAt) {
        return builder()
                .signalStrength(signalStrength)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ContactRecorderSignalStrengthHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder signalStrength(long signalStrength);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract ContactRecorderSignalStrengthHistogramEntry build();
    }

}
