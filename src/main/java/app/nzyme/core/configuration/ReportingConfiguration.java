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

package app.nzyme.core.configuration;

import com.google.auto.value.AutoValue;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.config.TransportStrategy;

import javax.annotation.Nullable;

@AutoValue
public abstract class ReportingConfiguration {

    @Nullable
    public abstract Email email();

    public static ReportingConfiguration create(Email email) {
        return builder()
                .email(email)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReportingConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder email(Email email);

        public abstract ReportingConfiguration build();
    }

    @AutoValue
    public static abstract class Email {

        public abstract TransportStrategy transportStrategy();
        public abstract String host();
        public abstract int port();
        public abstract String username();
        public abstract String password();
        public abstract Recipient from();
        public abstract String subjectPrefix();

        public static Email create(TransportStrategy transportStrategy, String host, int port, String username, String password, Recipient from, String subjectPrefix) {
            return builder()
                    .transportStrategy(transportStrategy)
                    .host(host)
                    .port(port)
                    .username(username)
                    .password(password)
                    .from(from)
                    .subjectPrefix(subjectPrefix)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_ReportingConfiguration_Email.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder transportStrategy(TransportStrategy transportStrategy);

            public abstract Builder host(String host);

            public abstract Builder port(int port);

            public abstract Builder username(String username);

            public abstract Builder password(String password);

            public abstract Builder from(Recipient from);

            public abstract Builder subjectPrefix(String subjectPrefix);

            public abstract Email build();
        }

    }

}
