package app.nzyme.core.ethernet.beacons;

import app.nzyme.core.ethernet.CIDR;
import app.nzyme.core.ethernet.beacons.db.BeaconExclusionEntry;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import jakarta.annotation.Nullable;

import java.net.InetAddress;
import java.util.*;
import java.util.regex.Pattern;

public class BeaconExclusionMatcher {

    public record PortRange(int from, int to) {
        public boolean contains(int port) {
            return port >= from && port <= to;
        }
    }

    private static final Pattern HOST_PATTERN_CHARACTERS = Pattern.compile("^[a-zA-Z0-9.*_-]+$");
    private static final Pattern MAC_PATTERN = Pattern.compile("^([0-9A-F]{2}:){5}[0-9A-F]{2}$");

    @Nullable private final CIDR destinationCidr;
    @Nullable private final String destinationHostPattern;
    @Nullable private final Pattern destinationHostRegex;
    @Nullable private final List<PortRange> destinationPorts;
    @Nullable private final Set<BeaconType> beaconTypes;
    @Nullable private final CIDR sourceCidr;
    @Nullable private final String sourceMac;

    private BeaconExclusionMatcher(@Nullable CIDR destinationCidr,
                                   @Nullable String destinationHostPattern,
                                   @Nullable List<PortRange> destinationPorts,
                                   @Nullable Set<BeaconType> beaconTypes,
                                   @Nullable CIDR sourceCidr,
                                   @Nullable String sourceMac) {
        this.destinationCidr = destinationCidr;
        this.destinationHostPattern = destinationHostPattern;
        this.destinationHostRegex = destinationHostPattern == null ? null : hostPatternToRegex(destinationHostPattern);
        this.destinationPorts = destinationPorts;
        this.beaconTypes = beaconTypes;
        this.sourceCidr = sourceCidr;
        this.sourceMac = sourceMac;
    }

    public static BeaconExclusionMatcher create(@Nullable String destinationCidr,
                                                @Nullable String destinationHost,
                                                @Nullable String destinationPorts,
                                                @Nullable Collection<String> beaconTypes,
                                                @Nullable String sourceCidr,
                                                @Nullable String sourceMac) {
        CIDR dstCidr = parseCidr(destinationCidr, "Destination address");
        String hostPattern = normalizeHostPattern(destinationHost);
        List<PortRange> ports = parsePorts(destinationPorts);
        Set<BeaconType> types = parseTypes(beaconTypes);
        CIDR srcCidr = parseCidr(sourceCidr, "Device address");
        String mac = normalizeMac(sourceMac);

        if (dstCidr == null && hostPattern == null && ports == null && types == null && srcCidr == null && mac == null) {
            throw new IllegalArgumentException("At least one criterion is required.");
        }

        return new BeaconExclusionMatcher(dstCidr, hostPattern, ports, types, srcCidr, mac);
    }

    public static BeaconExclusionMatcher create(BeaconExclusionEntry entry) {
        return create(
                entry.destinationCidr(),
                entry.destinationHost(),
                entry.destinationPorts(),
                entry.beaconTypes(),
                entry.sourceCidr(),
                entry.sourceMac()
        );
    }

    public boolean hasSourceCriteria() {
        return sourceCidr != null || sourceMac != null;
    }

    public boolean matchesDestination(@Nullable String type,
                                      @Nullable String host,
                                      @Nullable String address,
                                      @Nullable Integer port) {
        if (beaconTypes != null) {
            BeaconType t;
            try {
                t = type == null ? null : BeaconType.valueOf(type);
            } catch (IllegalArgumentException e) {
                t = null;
            }

            if (t == null || !beaconTypes.contains(t)) {
                return false;
            }
        }

        if (destinationHostRegex != null && !hostMatches(host)) {
            return false;
        }

        if (destinationCidr != null && !cidrContains(destinationCidr, address)) {
            return false;
        }

        if (destinationPorts != null) {
            if (port == null) {
                return false;
            }

            boolean any = false;
            for (PortRange range : destinationPorts) {
                if (range.contains(port)) {
                    any = true;
                    break;
                }
            }

            if (!any) {
                return false;
            }
        }

        return true;
    }

    public boolean matchesSource(@Nullable String address, @Nullable String mac) {
        if (sourceCidr != null && !cidrContains(sourceCidr, address)) {
            return false;
        }

        if (sourceMac != null && (mac == null || !sourceMac.equalsIgnoreCase(mac.trim()))) {
            return false;
        }

        return true;
    }

    public boolean matches(@Nullable String type,
                           @Nullable String destinationHost,
                           @Nullable String destinationAddress,
                           @Nullable Integer destinationPort,
                           @Nullable String sourceAddress,
                           @Nullable String sourceMac) {
        return matchesDestination(type, destinationHost, destinationAddress, destinationPort)
                && matchesSource(sourceAddress, sourceMac);
    }

    private boolean hostMatches(@Nullable String host) {
        if (host == null || destinationHostRegex == null) {
            return false;
        }

        String normalized = host.trim().toLowerCase();
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (destinationHostRegex.matcher(normalized).matches()) {
            return true;
        }

        // "*.example.com" also covers "example.com" itself. That is what users expect from a domain exclusion.
        return destinationHostPattern.startsWith("*.") && normalized.equals(destinationHostPattern.substring(2));
    }

    @Nullable
    static CIDR parseCidr(@Nullable String s, String label) {
        if (s == null || s.isBlank()) {
            return null;
        }

        String trimmed = s.trim();
        try {
            return CIDR.parse(trimmed);
        } catch (IllegalArgumentException e) {
            String reason = e.getMessage() != null && e.getMessage().startsWith("Host bits set")
                    ? "has host bits set. Use the network address, like 10.0.0.0/8."
                    : "is not a valid IP address or CIDR network.";
            throw new IllegalArgumentException(label + " [" + trimmed + "] " + reason);
        }
    }

    @Nullable
    public static String normalizeHostPattern(@Nullable String s) {
        if (s == null || s.isBlank()) {
            return null;
        }

        String pattern = s.trim().toLowerCase();
        if (pattern.endsWith(".")) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        if (pattern.length() > 253 || !HOST_PATTERN_CHARACTERS.matcher(pattern).matches()) {
            throw new IllegalArgumentException("Destination host [" + s.trim() + "] is not a valid host name " +
                    "pattern. Use letters, digits, dots, hyphens and * as wildcard.");
        }

        if (pattern.equals("*") || pattern.replace("*", "").replace(".", "").isEmpty()) {
            throw new IllegalArgumentException("Destination host pattern [" + s.trim() + "] would match every host.");
        }

        return pattern;
    }

    @Nullable
    public static List<PortRange> parsePorts(@Nullable String s) {
        if (s == null || s.isBlank()) {
            return null;
        }

        List<PortRange> result = Lists.newArrayList();
        for (String part : s.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }

            try {
                if (p.contains("-")) {
                    String[] bounds = p.split("-", -1);
                    if (bounds.length != 2) {
                        throw new NumberFormatException();
                    }
                    int from = Integer.parseInt(bounds[0].trim());
                    int to = Integer.parseInt(bounds[1].trim());
                    if (from < 0 || to > 65535 || from > to) {
                        throw new NumberFormatException();
                    }
                    result.add(new PortRange(from, to));
                } else {
                    int port = Integer.parseInt(p);
                    if (port < 0 || port > 65535) {
                        throw new NumberFormatException();
                    }
                    result.add(new PortRange(port, port));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Port [" + p + "] is not a valid port or port range. Use " +
                        "single ports (443) and ranges (8000-8100), separated by commas.");
            }
        }

        return result.isEmpty() ? null : result;
    }

    @Nullable
    static Set<BeaconType> parseTypes(@Nullable Collection<String> types) {
        if (types == null) {
            return null;
        }

        Set<BeaconType> result = EnumSet.noneOf(BeaconType.class);
        for (String type : types) {
            if (type == null || type.isBlank()) {
                continue;
            }

            try {
                result.add(BeaconType.valueOf(type.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown beacon type [" + type + "].");
            }
        }

        return result.isEmpty() ? null : result;
    }

    @Nullable
    public static String normalizeMac(@Nullable String s) {
        if (s == null || s.isBlank()) {
            return null;
        }

        String mac = s.trim().toUpperCase().replace("-", ":");
        if (!MAC_PATTERN.matcher(mac).matches()) {
            throw new IllegalArgumentException("Device MAC address [" + s.trim() + "] is not a valid MAC address.");
        }

        return mac;
    }

    @Nullable
    public static String normalizeCidr(@Nullable String s) {
        CIDR cidr = parseCidr(s, "Address");
        if (cidr == null) {
            return null;
        }

        return cidr.isSingleHost() ? InetAddresses.toAddrString(cidr.address()) : cidr.toString();
    }

    @Nullable
    public static String normalizePorts(@Nullable String s) {
        List<PortRange> ranges = parsePorts(s);
        if (ranges == null) {
            return null;
        }

        List<String> parts = Lists.newArrayList();
        for (PortRange r : ranges) {
            parts.add(r.from() == r.to() ? String.valueOf(r.from()) : r.from() + "-" + r.to());
        }

        return String.join(",", parts);
    }

    static boolean cidrContains(CIDR cidr, @Nullable String address) {
        if (address == null || !BeaconDetector.isValidAddress(address)) {
            return false;
        }

        InetAddress addr;
        try {
            addr = InetAddresses.forString(address);
        } catch (IllegalArgumentException e) {
            return false;
        }

        byte[] a = addr.getAddress();
        byte[] n = cidr.address().getAddress();
        if (a.length != n.length) {
            return false;
        }

        int prefix = cidr.prefix();
        for (int i = 0; i < prefix; i++) {
            int mask = 1 << (7 - i % 8);
            if ((a[i / 8] & mask) != (n[i / 8] & mask)) {
                return false;
            }
        }

        return true;
    }

    private static Pattern hostPatternToRegex(String pattern) {
        StringBuilder sb = new StringBuilder("^");
        for (String literal : pattern.split("\\*", -1)) {
            sb.append(Pattern.quote(literal)).append(".*");
        }
        // Remove the trailing ".*" that the split loop appended after the last literal.
        sb.setLength(sb.length() - 2);
        sb.append("$");

        return Pattern.compile(sb.toString());
    }

}
