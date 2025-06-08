package app.nzyme.core.ethernet.dhcp;

import com.google.common.hash.Hashing;
import jakarta.annotation.Nullable;

import java.nio.charset.StandardCharsets;
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
            String c;
            if (option == 51 || option == 54) {
                c = "X";
            } else {
                c = String.valueOf(option);
            }
            fingerprint.append(c).append("|");
        }

        if (vendorClass != null && !vendorClass.trim().isEmpty()) {
            fingerprint.append(vendorClass);
        }

        // In case we only had filtered out options.
        if (fingerprint.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                Hashing.sha256()
                        .hashBytes(fingerprint.toString().getBytes(StandardCharsets.UTF_8))
                        .toString());
    }

}
