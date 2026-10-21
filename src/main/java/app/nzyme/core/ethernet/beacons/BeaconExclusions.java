package app.nzyme.core.ethernet.beacons;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.util.filters.FilterSql;
import app.nzyme.core.util.filters.FilterSqlFragment;
import app.nzyme.core.util.filters.Filters;
import app.nzyme.core.ethernet.beacons.db.BeaconEntry;
import app.nzyme.core.ethernet.beacons.db.BeaconExclusionEntry;
import app.nzyme.core.ethernet.beacons.db.BeaconSourceEntry;
import com.google.common.collect.Lists;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;

import java.util.*;

public class BeaconExclusions {

    private static final Logger LOG = LogManager.getLogger(BeaconExclusions.class);

    private static final int IMPACT_SAMPLE_SIZE = 25;

    private final NzymeNode nzyme;
    private final Beacons beacons;

    public BeaconExclusions(NzymeNode nzyme, Beacons beacons) {
        this.nzyme = nzyme;
        this.beacons = beacons;
    }

    public record Criteria(@Nullable String destinationCidr,
                           @Nullable String destinationHost,
                           @Nullable String destinationPorts,
                           @Nullable List<String> beaconTypes,
                           @Nullable String sourceCidr,
                           @Nullable String sourceMac) {
        public Criteria normalized() {
            BeaconExclusionMatcher.create(destinationCidr, destinationHost, destinationPorts, beaconTypes,
                    sourceCidr, sourceMac);

            List<String> types = null;
            Set<BeaconType> parsed = BeaconExclusionMatcher.parseTypes(beaconTypes);
            if (parsed != null) {
                types = Lists.newArrayList();
                for (BeaconType t : BeaconType.values()) {
                    if (parsed.contains(t)) {
                        types.add(t.name());
                    }
                }
            }

            return new Criteria(
                    BeaconExclusionMatcher.normalizeCidr(destinationCidr),
                    BeaconExclusionMatcher.normalizeHostPattern(destinationHost),
                    BeaconExclusionMatcher.normalizePorts(destinationPorts),
                    types,
                    BeaconExclusionMatcher.normalizeCidr(sourceCidr),
                    BeaconExclusionMatcher.normalizeMac(sourceMac)
            );
        }

        public BeaconExclusionMatcher matcher() {
            return BeaconExclusionMatcher.create(destinationCidr, destinationHost, destinationPorts, beaconTypes,
                    sourceCidr, sourceMac);
        }
    }

    public enum OrderColumn {

        NAME("name"),
        ENABLED("enabled"),
        LAST_MATCHED_AT("last_matched_at"),
        MATCH_COUNT("match_count"),
        CREATED_AT("created_at"),
        UPDATED_AT("updated_at");

        private final String columnName;

        OrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }
    }

    public long count(Filters filters, UUID organizationId, UUID tenantId) {
        FilterSqlFragment filterFragment = FilterSql.generate(filters, new BeaconExclusionFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM beacon_exclusions WHERE organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id " + filterFragment.whereSql())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bindMap(filterFragment.bindings())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<BeaconExclusionEntry> findAll(Filters filters,
                                              OrderColumn orderColumn,
                                              OrderDirection orderDirection,
                                              int limit,
                                              int offset,
                                              UUID organizationId,
                                              UUID tenantId) {
        FilterSqlFragment filterFragment = FilterSql.generate(filters, new BeaconExclusionFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM beacon_exclusions WHERE organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id " + filterFragment.whereSql() +
                                "ORDER BY <order_column> <order_direction> NULLS LAST, name ASC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bindMap(filterFragment.bindings())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .define("order_column", orderColumn.getColumnName())
                        .define("order_direction", orderDirection)
                        .mapTo(BeaconExclusionEntry.class)
                        .list()
        );
    }

    public List<BeaconExclusionEntry> findAllEnabled(Handle handle, UUID organizationId, UUID tenantId) {
        return handle.createQuery("SELECT * FROM beacon_exclusions WHERE organization_id = :organization_id " +
                        "AND tenant_id = :tenant_id AND enabled = true ORDER BY name ASC")
                .bind("organization_id", organizationId)
                .bind("tenant_id", tenantId)
                .mapTo(BeaconExclusionEntry.class)
                .list();
    }

    public Optional<BeaconExclusionEntry> find(UUID uuid, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM beacon_exclusions WHERE uuid = :uuid " +
                                "AND organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("uuid", uuid)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(BeaconExclusionEntry.class)
                        .findOne()
        );
    }

    public BeaconExclusionEntry create(UUID organizationId,
                                       UUID tenantId,
                                       String name,
                                       @Nullable String description,
                                       boolean enabled,
                                       Criteria criteria,
                                       @Nullable UUID createdByUserId,
                                       @Nullable String createdByName,
                                       @Nullable String createdByEmail) {
        Criteria c = criteria.normalized();

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO beacon_exclusions(uuid, organization_id, tenant_id, name, " +
                                "description, enabled, destination_cidr, destination_host, destination_ports, " +
                                "beacon_types, source_cidr, source_mac, match_count, last_matched_at, " +
                                "created_by_user_id, created_by_name, created_by_email, created_at, updated_at) " +
                                "VALUES(:uuid, :organization_id, :tenant_id, :name, :description, :enabled, " +
                                ":destination_cidr, :destination_host, :destination_ports, :beacon_types, " +
                                ":source_cidr, :source_mac, 0, NULL, :created_by_user_id, :created_by_name, " +
                                ":created_by_email, NOW(), NOW()) RETURNING *")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("enabled", enabled)
                        .bind("destination_cidr", c.destinationCidr())
                        .bind("destination_host", c.destinationHost())
                        .bind("destination_ports", c.destinationPorts())
                        .bind("beacon_types", c.beaconTypes() == null ? null : String.join(",", c.beaconTypes()))
                        .bind("source_cidr", c.sourceCidr())
                        .bind("source_mac", c.sourceMac())
                        .bind("created_by_user_id", createdByUserId)
                        .bind("created_by_name", createdByName)
                        .bind("created_by_email", createdByEmail)
                        .mapTo(BeaconExclusionEntry.class)
                        .one()
        );
    }

    public void update(long id, String name, @Nullable String description, boolean enabled, Criteria criteria) {
        Criteria c = criteria.normalized();

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE beacon_exclusions SET name = :name, description = :description, " +
                                "enabled = :enabled, destination_cidr = :destination_cidr, " +
                                "destination_host = :destination_host, destination_ports = :destination_ports, " +
                                "beacon_types = :beacon_types, source_cidr = :source_cidr, source_mac = :source_mac, " +
                                "updated_at = NOW() WHERE id = :id")
                        .bind("id", id)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("enabled", enabled)
                        .bind("destination_cidr", c.destinationCidr())
                        .bind("destination_host", c.destinationHost())
                        .bind("destination_ports", c.destinationPorts())
                        .bind("beacon_types", c.beaconTypes() == null ? null : String.join(",", c.beaconTypes()))
                        .bind("source_cidr", c.sourceCidr())
                        .bind("source_mac", c.sourceMac())
                        .execute()
        );
    }

    public void setEnabled(long id, boolean enabled) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE beacon_exclusions SET enabled = :enabled, updated_at = NOW() " +
                                "WHERE id = :id")
                        .bind("id", id)
                        .bind("enabled", enabled)
                        .execute()
        );
    }

    public void delete(long id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM beacon_exclusions WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public void recordMatches(Handle handle, long id, long count) {
        handle.createUpdate("UPDATE beacon_exclusions SET match_count = match_count + :count, " +
                        "last_matched_at = NOW() WHERE id = :id")
                .bind("id", id)
                .bind("count", count)
                .execute();
    }

    public Map<BeaconExclusionEntry, BeaconExclusionMatcher> compileEnabled(Handle handle,
                                                                           UUID organizationId,
                                                                           UUID tenantId) {
        Map<BeaconExclusionEntry, BeaconExclusionMatcher> result = new LinkedHashMap<>();
        for (BeaconExclusionEntry entry : findAllEnabled(handle, organizationId, tenantId)) {
            try {
                result.put(entry, BeaconExclusionMatcher.create(entry));
            } catch (IllegalArgumentException e) {
                LOG.error("Beacon exclusion [{}] of tenant [{}/{}] has invalid criteria and is ignored: {}",
                        entry.uuid(), organizationId, tenantId, e.getMessage());
            }
        }

        return result;
    }

    public record MatchedBeacon(UUID uuid,
                                String name,
                                String type,
                                String destination,
                                int matchingSources,
                                int totalSources) {
        public boolean complete() {
            return matchingSources >= totalSources;
        }
    }

    public record Impact(int matchingBeacons,
                         int completeBeacons,
                         int matchingSources,
                         List<MatchedBeacon> beacons) {
        public static Impact empty() {
            return new Impact(0, 0, 0, Collections.emptyList());
        }
    }

    public Impact calculateImpact(BeaconExclusionMatcher matcher, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle -> matchExisting(handle, matcher, organizationId, tenantId));
    }

    public Impact applyToExistingBeacons(BeaconExclusionMatcher matcher, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().inTransaction(handle -> {
            Impact impact = matchExisting(handle, matcher, organizationId, tenantId);

            for (BeaconEntry beacon : beacons.findAllBeaconsOfTenant(handle, organizationId, tenantId)) {
                if (!matcher.matchesDestination(beacon.type(), beacon.destinationHost(), beacon.destinationAddress(),
                        beacon.destinationPort())) {
                    continue;
                }

                if (!matcher.hasSourceCriteria()) {
                    beacons.deleteBeaconById(handle, beacon.id());
                    continue;
                }

                List<BeaconSourceEntry> sources = beacons.findAllSourcesOfBeacons(handle, List.of(beacon.id()));
                List<Long> matching = Lists.newArrayList();
                for (BeaconSourceEntry source : sources) {
                    if (matcher.matchesSource(source.sourceAddress(), source.sourceMac())) {
                        matching.add(source.id());
                    }
                }

                if (matching.isEmpty()) {
                    continue;
                }

                if (matching.size() >= sources.size()) {
                    beacons.deleteBeaconById(handle, beacon.id());
                } else {
                    beacons.deleteSources(handle, matching);
                    beacons.refreshBeaconAggregates(handle, beacon.id(), null, null);
                }
            }

            return impact;
        });
    }

    private Impact matchExisting(Handle handle, BeaconExclusionMatcher matcher, UUID organizationId, UUID tenantId) {
        List<BeaconEntry> all = beacons.findAllBeaconsOfTenant(handle, organizationId, tenantId);

        List<BeaconEntry> destinationMatches = Lists.newArrayList();
        for (BeaconEntry beacon : all) {
            if (matcher.matchesDestination(beacon.type(), beacon.destinationHost(), beacon.destinationAddress(),
                    beacon.destinationPort())) {
                destinationMatches.add(beacon);
            }
        }

        if (destinationMatches.isEmpty()) {
            return Impact.empty();
        }

        Map<Long, List<BeaconSourceEntry>> sourcesByBeacon = new HashMap<>();
        for (BeaconSourceEntry source : beacons.findAllSourcesOfBeacons(handle,
                destinationMatches.stream().map(BeaconEntry::id).toList())) {
            sourcesByBeacon.computeIfAbsent(source.beaconId(), k -> Lists.newArrayList()).add(source);
        }

        List<MatchedBeacon> matched = Lists.newArrayList();
        int matchingSources = 0;
        int completeBeacons = 0;
        for (BeaconEntry beacon : destinationMatches) {
            List<BeaconSourceEntry> sources = sourcesByBeacon.getOrDefault(beacon.id(), Collections.emptyList());

            int matching;
            if (!matcher.hasSourceCriteria()) {
                matching = sources.size();
            } else {
                matching = 0;
                for (BeaconSourceEntry source : sources) {
                    if (matcher.matchesSource(source.sourceAddress(), source.sourceMac())) {
                        matching++;
                    }
                }

                if (matching == 0) {
                    continue;
                }
            }

            MatchedBeacon mb = new MatchedBeacon(
                    beacon.uuid(),
                    beacon.name(),
                    beacon.type(),
                    BeaconDetector.destinationDescription(beacon),
                    matching,
                    sources.size()
            );

            matched.add(mb);
            matchingSources += matching;
            if (mb.complete()) {
                completeBeacons++;
            }
        }

        // Complete matches first, then by name. Limit the sample.
        matched.sort(Comparator.comparing((MatchedBeacon m) -> !m.complete()).thenComparing(MatchedBeacon::name));
        List<MatchedBeacon> sample = matched.size() > IMPACT_SAMPLE_SIZE
                ? matched.subList(0, IMPACT_SAMPLE_SIZE) : matched;

        return new Impact(matched.size(), completeBeacons, matchingSources, Lists.newArrayList(sample));
    }

    public List<BeaconExclusionEntry> findMatchingExclusions(BeaconEntry beacon,
                                                             Map<BeaconExclusionEntry, BeaconExclusionMatcher> compiled) {
        List<BeaconExclusionEntry> result = Lists.newArrayList();
        for (Map.Entry<BeaconExclusionEntry, BeaconExclusionMatcher> e : compiled.entrySet()) {
            if (e.getValue().matchesDestination(beacon.type(), beacon.destinationHost(), beacon.destinationAddress(),
                    beacon.destinationPort())) {
                result.add(e.getKey());
            }
        }

        return result;
    }

    public Map<BeaconExclusionEntry, BeaconExclusionMatcher> compileEnabled(UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle -> compileEnabled(handle, organizationId, tenantId));
    }

}
