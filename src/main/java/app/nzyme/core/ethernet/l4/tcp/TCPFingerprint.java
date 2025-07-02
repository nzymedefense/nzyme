package app.nzyme.core.ethernet.l4.tcp;

import com.google.common.hash.Hashing;
import jakarta.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TCPFingerprint {

    private final int ipTtl;
    private final int ipTos;
    private final boolean ipDf;
    private final int windowSize;
    @Nullable
    private final Integer maximumSegmentSize;
    @Nullable
    private final Integer windowScaleMultiplier;
    private final List<Integer> options;

    public TCPFingerprint(int ipTtl,
                          int ipTos,
                          boolean ipDf,
                          int windowSize,
                          @Nullable Integer maximumSegmentSize,
                          @Nullable Integer windowScaleMultiplier,
                          List<Integer> options) {
        this.ipTtl = ipTtl;
        this.ipTos = ipTos;
        this.ipDf = ipDf;
        this.windowSize = windowSize;
        this.maximumSegmentSize = maximumSegmentSize;
        this.windowScaleMultiplier = windowScaleMultiplier;
        this.options = options;
    }

    public String generate() {
        List<String> parts = new ArrayList<>();
        parts.add(String.valueOf(ipTtl));
        parts.add(String.valueOf(ipTos));
        parts.add(ipDf ? "1" : "0");
        parts.add(String.valueOf(windowSize));
        parts.add(maximumSegmentSize != null ? String.valueOf(maximumSegmentSize) : "0");
        parts.add(windowScaleMultiplier != null ? String.valueOf(windowScaleMultiplier) : "0");

        // Append each option byte.
        for (Integer opt : options) {
            parts.add(String.valueOf(opt));
        }

        // Join.
        StringJoiner sj = new StringJoiner("|");
        for (String p : parts) {
            sj.add(p);
        }

        // Hash.
        return Hashing
                .sha256()
                .hashBytes(sj.toString().getBytes(StandardCharsets.UTF_8))
                .toString();
    }

}
