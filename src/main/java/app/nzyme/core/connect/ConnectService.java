package app.nzyme.core.connect;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.RegistryCryptoException;
import com.google.common.base.Strings;
import jakarta.annotation.Nullable;

import java.net.URI;
import java.util.Optional;

public class ConnectService {

    private final NzymeNode nzyme;

    private static final String DEFAULT_API_URI = "https://connect.nzyme.org/";

    public ConnectService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public boolean isEnabled() {
        Optional<String> enabled = nzyme.getDatabaseCoreRegistry().getValue(ConnectRegistryKeys.CONNECT_ENABLED.key());

        return enabled.isPresent() && enabled.get().equals("true") && getApiKey() != null;
    }

    public URI getApiUri() {
        if (Strings.isNullOrEmpty(nzyme.getConfiguration().connectApiUri())) {
            return URI.create(DEFAULT_API_URI);
        } else {
            return URI.create(nzyme.getConfiguration().connectApiUri());
        }
    }

    @Nullable
    public String getApiKey() {
        Optional<String> apiKey;
        try {
            apiKey = nzyme.getDatabaseCoreRegistry().getEncryptedValue(ConnectRegistryKeys.CONNECT_API_KEY.key());
        } catch(RegistryCryptoException e) {
            throw new RuntimeException("Could not decrypt Connect API key.", e);
        }

        return apiKey.orElse(null);
    }

}
