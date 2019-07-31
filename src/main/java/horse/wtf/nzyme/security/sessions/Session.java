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

import io.jsonwebtoken.Jwts;
import org.joda.time.DateTime;

import java.security.Key;

public class Session {

    public static String createToken(String username, Key signingKey) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("nzyme")
                .setIssuedAt(DateTime.now().toDate())
                .setExpiration(DateTime.now().plusHours(8).toDate())
                .signWith(signingKey)
                .compact();
    }

}
