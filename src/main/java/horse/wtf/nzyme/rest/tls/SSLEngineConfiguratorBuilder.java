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

import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.cert.CertificateException;

// BASED ON THE GPLv3-LICENSED GRAYLOG CODE.

public class SSLEngineConfiguratorBuilder {

    public static SSLEngineConfigurator build(Path certFile, Path keyFile)
            throws GeneralSecurityException, IOException {
        if (keyFile == null || !Files.isRegularFile(keyFile) || !Files.isReadable(keyFile)) {
            throw new InvalidKeyException("Unreadable or missing private key: " + keyFile);
        }

        if (certFile == null || !Files.isRegularFile(certFile) || !Files.isReadable(certFile)) {
            throw new CertificateException("Unreadable or missing X.509 certificate: " + certFile);
        }

        final SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator();
        final char[] password = "".toCharArray(); // we might want to support passwords in the future.
        final KeyStore keyStore = PemKeyStore.buildKeyStore(certFile, keyFile, password);
        sslContextConfigurator.setKeyStorePass(password);
        sslContextConfigurator.setKeyStoreBytes(KeyStoreUtils.getBytes(keyStore, password));

        final SSLContext sslContext = sslContextConfigurator.createSSLContext(true);
        return new SSLEngineConfigurator(sslContext, false, false, false);
    }

}
