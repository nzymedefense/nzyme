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
