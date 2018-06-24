/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
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
