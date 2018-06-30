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

package horse.wtf.nzyme.configuration;

import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.Validator;

public class InternetAddressValidator implements Validator<String> {

    @Override
    public void validate(String name, String value) throws ValidationException {
        for (String s : value.split(",")) {
            if(!s.contains(":") || s.startsWith(":") || s.endsWith(":")) {
                throw new ValidationException("Malformed internet address.");
            }

            String[] parts = s.split(":");

            if(parts.length != 2) {
                throw new ValidationException("Malformed internet address");
            }

            try {
                int port = Integer.valueOf(parts[1]);

                if (port <= 0 || port > 65535) {
                    throw new ValidationException("Invalid port number.");
                }
            } catch(NumberFormatException e) {
                throw new ValidationException("Port is not a number");
            }
        }
    }

}
