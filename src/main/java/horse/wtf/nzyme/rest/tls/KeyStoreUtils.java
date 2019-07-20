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

// BASED ON THE GPLv3-LICENSED GRAYLOG CODE.

package horse.wtf.nzyme.rest.tls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class KeyStoreUtils {

    private KeyStoreUtils() {
    }

    public static byte[] getBytes(KeyStore keyStore, char[] password) throws GeneralSecurityException, IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        keyStore.store(stream, password);

        return stream.toByteArray();
    }

}
