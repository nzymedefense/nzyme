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

import java.util.UUID;

@AutoValue
public abstract class ContactRecord {

    public abstract UUID contactUUID();
    public abstract ContactRecorder.RECORD_TYPE recordType();
    public abstract String recordValue();
    public abstract long frameCount();
    public abstract double rssiAverage();
    public abstract double rssiStdDev();
    public abstract DateTime createdAt();

    public static ContactRecord create(UUID contactUUID, ContactRecorder.RECORD_TYPE recordType, String recordValue, long frameCount, double rssiAverage, double rssiStdDev, DateTime createdAt) {
        return builder()
                .contactUUID(contactUUID)
                .recordType(recordType)
                .recordValue(recordValue)
                .frameCount(frameCount)
                .rssiAverage(rssiAverage)
                .rssiStdDev(rssiStdDev)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ContactRecord.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder contactUUID(UUID contactUUID);

        public abstract Builder recordType(ContactRecorder.RECORD_TYPE recordType);

        public abstract Builder recordValue(String recordValue);

        public abstract Builder frameCount(long frameCount);

        public abstract Builder rssiAverage(double rssiAverage);

        public abstract Builder rssiStdDev(double rssiStdDev);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract ContactRecord build();
    }

}
