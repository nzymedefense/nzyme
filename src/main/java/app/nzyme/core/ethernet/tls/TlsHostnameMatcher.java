package app.nzyme.core.ethernet.tls;

import java.util.List;

public class TlsHostnameMatcher {

    public static boolean matches(String hostname, CertificateDetails leaf) {
        if (hostname == null || hostname.isEmpty() || !leaf.parsed()) {
            return false;
        }

        String host = hostname.toLowerCase().replaceAll("\\.$", "");

        List<String> names = leaf.dnsNames();
        if (names.isEmpty() && leaf.commonName() != null) {
            names = List.of(leaf.commonName().toLowerCase());
        }

        for (String name : names) {
            if (matchesName(host, name)) {
                return true;
            }
        }

        return false;
    }

    static boolean matchesName(String host, String pattern) {
        pattern = pattern.replaceAll("\\.$", "");

        if (!pattern.startsWith("*.")) {
            return host.equals(pattern);
        }

        String suffix = pattern.substring(1); // ".example.com"
        if (!host.endsWith(suffix) || host.length() <= suffix.length()) {
            return false;
        }

        String leftmost = host.substring(0, host.length() - suffix.length());
        return !leftmost.isEmpty() && !leftmost.contains(".");
    }

}
