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

package horse.wtf.nzyme.security.transport;

import com.google.crypto.tink.subtle.AesGcmJce;

import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;

public class TransportEncryption {

    /*
     * The tink classes used here are marked as not stable and might change at any time. However, this is the only
     * way to do symmetric encryption with a passphrase in tink and we have unit tests around our wrapper.
     *
     * In the future we might want to also offer key based encryption but that will require a user to move keys around
     * between nodes that are communicating on an encrypted transport.
     */

    private final AesGcmJce encryption;

    public TransportEncryption(@NotNull String key) throws GeneralSecurityException, IllegalArgumentException {
        if (key.getBytes().length != 32) { // Key has to be 256 bit length.
            throw new IllegalArgumentException("Key must be 256 bit long. (32 characters)");
        }

        encryption = new AesGcmJce(key.getBytes());
    }

    public byte[] encrypt(byte[] payload) throws GeneralSecurityException {
        return encryption.encrypt(payload, null);
    }

    public byte[] decrypt(byte[] payload) throws GeneralSecurityException {
        return encryption.decrypt(payload, null);
    }

}
