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

package horse.wtf.nzyme.dot11.deauth.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DeauthenticationMonitorRecording {

    public abstract Long totalFrameCount();
    public abstract DateTime createdAt();

    public static DeauthenticationMonitorRecording create(Long totalFrameCount, DateTime createdAt) {
        return builder()
                .totalFrameCount(totalFrameCount)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DeauthenticationMonitorRecording.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder totalFrameCount(Long totalFrameCount);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract DeauthenticationMonitorRecording build();
    }

}
