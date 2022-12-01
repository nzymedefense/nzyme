package horse.wtf.nzyme.rest.authentication;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HTTPBasicAuthParser {

    private static final Logger LOG = LogManager.getLogger(HTTPBasicAuthParser.class);

    public static Credentials parse(String header) throws IllegalArgumentException {
        if (!header.startsWith("Basic ")) {
            throw new IllegalArgumentException("Invalid header format: Scheme");
        }

        String[] parts = header.split("Basic ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid header format: Credentials");
        }

        String credsValue = new String(BaseEncoding.base64().decode(parts[1]), Charsets.UTF_8);

        if (credsValue.startsWith(":") || credsValue.endsWith(":") || !credsValue.contains(":")) {
            throw new IllegalArgumentException("Invalid authorization payload");
        }

        String[] pair = credsValue.split(":");

        if (Strings.isNullOrEmpty(pair[0]) || Strings.isNullOrEmpty(pair[1])) {
            throw new IllegalArgumentException("Invalid authorization payload: Credential format");
        }

        return new Credentials(pair[0].trim(), pair[1].trim());
    }

    public static final class Credentials {

        private final String username;
        private final String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

    }

}
