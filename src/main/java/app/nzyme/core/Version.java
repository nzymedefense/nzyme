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

package app.nzyme.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {

    private static final Logger LOG = LogManager.getLogger(Version.class);

    public String getVersionString() {
        Properties gitProperties = new Properties();
        Properties buildProperties = new Properties();

        try {
            InputStream git = getClass().getClassLoader().getResourceAsStream("git.properties");
            InputStream build = getClass().getClassLoader().getResourceAsStream("build.properties");

            if (git == null || build == null) {
                throw new RuntimeException("git.properties or build.properties missing. Run mvn:package to fix.");
            }

            gitProperties.load(git);
            buildProperties.load(build);

            return new StringBuilder(String.valueOf(gitProperties.get("git.build.version")))
                    .append(" built at [")
                    .append(buildProperties.get("date")).append("]")
                    .toString();
        } catch (IOException e) {
            LOG.error("Could not load version information.", e);
            return "";
        }
    }

    public com.github.zafarkhaja.semver.Version getVersion() {
        try {
            Properties gitProperties = new Properties();
            gitProperties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));

            return com.github.zafarkhaja.semver.Version.valueOf(String.valueOf(gitProperties.get("git.build.version")));
        } catch(Exception e) {
            // This is not recoverable and can only happen if something goes sideways during build.
            throw new RuntimeException("Could not build semantic version from probe version.", e);
        }
    }

}
