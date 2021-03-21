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

package horse.wtf.nzyme.periodicals.versioncheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.zafarkhaja.semver.Version;
import org.joda.time.DateTime;

public class VersionResponse {

    @JsonProperty
    public String codename;

    @JsonProperty
    public CurrentVersion version;

    @JsonProperty("released_at")
    public DateTime releasedAt;

    public Version getVersion() {
        return Version.valueOf(version.major + "." + version.minor + "." + version.patch);
    }

    public String getFullVersionString() {
        StringBuilder sb = new StringBuilder();

        sb.append(version.major)
                .append(".")
                .append(version.minor)
                .append(".")
                .append(version.patch)
                .append(" (")
                .append(codename)
                .append(")");

        return sb.toString();
    }

}
