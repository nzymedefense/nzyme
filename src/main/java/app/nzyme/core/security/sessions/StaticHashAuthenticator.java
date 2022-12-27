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

package app.nzyme.core.security.sessions;

import com.google.common.hash.Hashing;
import app.nzyme.core.configuration.leader.LeaderConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public class StaticHashAuthenticator {

    private static final Logger LOG = LogManager.getLogger(StaticHashAuthenticator.class);

    private static final String USERNAME = "admin";

    private final String hash;

    public StaticHashAuthenticator(LeaderConfiguration configuration) {
        this.hash = configuration.adminPasswordHash();
    }

    public boolean authenticate(String username, String password) {
        if (username.equals(USERNAME)) {
            return hash.equals(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString());
        } else {
            LOG.debug("Username does not equal {}.", USERNAME);
            return false;
        }
    }

}
