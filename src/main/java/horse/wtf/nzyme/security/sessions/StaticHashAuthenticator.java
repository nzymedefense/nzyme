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

package horse.wtf.nzyme.security.sessions;

import com.google.common.hash.Hashing;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
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
