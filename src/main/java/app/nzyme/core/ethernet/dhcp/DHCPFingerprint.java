package app.nzyme.core.ethernet.dhcp;

import com.google.common.hash.Hashing;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;

public class DHCPFingerprint {

    private final List<Integer> options;

    @Nullable
    private final String vendorClass;

    public DHCPFingerprint(List<Integer> options, @Nullable String vendorClass) {
        this.options = options;
        this.vendorClass = vendorClass;
    }

    public Optional<String> generate() {
        if (options.isEmpty()) {
            return Optional.empty();
        }

        StringBuilder fingerprint = new StringBuilder();
        for (Integer option : options) {
            fingerprint.append(option);
        }

        if (vendorClass != null && !vendorClass.trim().isEmpty()) {
            fingerprint.append(vendorClass);
        }

        return Optional.of(
                Hashing.sha256()
                        .hashBytes(fingerprint.toString().getBytes())
                        .toString());
    }

}
