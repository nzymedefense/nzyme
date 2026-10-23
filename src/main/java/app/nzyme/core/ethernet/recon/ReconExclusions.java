package app.nzyme.core.ethernet.recon;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.ethernet.recon.db.ReconExclusionEntry;
import app.nzyme.core.ethernet.recon.db.ReconScanEntry;
import app.nzyme.core.util.filters.FilterSql;
import app.nzyme.core.util.filters.FilterSqlFragment;
import app.nzyme.core.util.filters.Filters;
import com.google.common.collect.Lists;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;

import java.util.*;

public class ReconExclusions {

    private static final Logger LOG = LogManager.getLogger(ReconExclusions.class);

    private static final int IMPACT_SAMPLE_SIZE = 25;
    private static final int TARGET_SAMPLE_SIZE = 5000;

    private final NzymeNode nzyme;
    private final Recon recon;

    public ReconExclusions(NzymeNode nzyme, Recon recon) {
        this.nzyme = nzyme;
        this.recon = recon;
    }

    public record Criteria(@Nullable List<String> scanTypes,
                           @Nullable String sourceCidr,
                           @Nullable String sourceMac,
                           @Nullable String targetCidr) {
        public Criteria normalized() {
            ReconExclusionMatcher.create(scanTypes, sourceCidr, sourceMac, targetCidr);

            List<String> types = null;
            Set<ReconScanType> parsed = ReconExclusionMatcher.parseTypes(scanTypes);
            if (parsed != null) {
                types = Lists.newArrayList();
                for (ReconScanType t : ReconScanType.values()) {
                    if (parsed.contains(t)) {
                        types.add(t.name());
                    }
                }
            }

            return new Criteria(
                    types,
                    ReconExclusionMatcher.normalizeCidr(sourceCidr),
                    ReconExclusionMatcher.normalizeMac(sourceMac),
                    ReconExclusionMatcher.normalizeCidr(targetCidr)
            );
        }

        public ReconExclusionMatcher matcher() {
            return ReconExclusionMatcher.create(scanTypes, sourceCidr, sourceMac, targetCidr);
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
        FilterSqlFragment filterFragment = FilterSql.generate(filters, new ReconExclusionFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM recon_exclusions WHERE organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id " + filterFragment.whereSql())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bindMap(filterFragment.bindings())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<ReconExclusionEntry> findAll(Filters filters,
                                             OrderColumn orderColumn,
                                             OrderDirection orderDirection,
                                             int limit,
                                             int offset,
                                             UUID organizationId,
                                             UUID tenantId) {
        FilterSqlFragment filterFragment = FilterSql.generate(filters, new ReconExclusionFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM recon_exclusions WHERE organization_id = :organization_id " +
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
                        .mapTo(ReconExclusionEntry.class)
                        .list()
        );
    }

    public List<ReconExclusionEntry> findAllEnabled(Handle handle, UUID organizationId, UUID tenantId) {
        return handle.createQuery("SELECT * FROM recon_exclusions WHERE organization_id = :organization_id " +
                        "AND tenant_id = :tenant_id AND enabled = true ORDER BY name ASC")
                .bind("organization_id", organizationId)
                .bind("tenant_id", tenantId)
                .mapTo(ReconExclusionEntry.class)
                .list();
    }

    public Optional<ReconExclusionEntry> find(UUID uuid, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM recon_exclusions WHERE uuid = :uuid " +
                                "AND organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("uuid", uuid)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(ReconExclusionEntry.class)
                        .findOne()
        );
    }

    public ReconExclusionEntry create(UUID organizationId,
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
                handle.createQuery("INSERT INTO recon_exclusions(uuid, organization_id, tenant_id, name, " +
                                "description, enabled, scan_types, source_cidr, source_mac, target_cidr, " +
                                "match_count, last_matched_at, created_by_user_id, created_by_name, " +
                                "created_by_email, created_at, updated_at) VALUES(:uuid, :organization_id, " +
                                ":tenant_id, :name, :description, :enabled, :scan_types, :source_cidr, " +
                                ":source_mac, :target_cidr, 0, NULL, :created_by_user_id, :created_by_name, " +
                                ":created_by_email, NOW(), NOW()) RETURNING *")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("enabled", enabled)
                        .bind("scan_types", c.scanTypes() == null ? null : String.join(",", c.scanTypes()))
                        .bind("source_cidr", c.sourceCidr())
                        .bind("source_mac", c.sourceMac())
                        .bind("target_cidr", c.targetCidr())
                        .bind("created_by_user_id", createdByUserId)
                        .bind("created_by_name", createdByName)
                        .bind("created_by_email", createdByEmail)
                        .mapTo(ReconExclusionEntry.class)
                        .one()
        );
    }

    public void update(long id, String name, @Nullable String description, boolean enabled, Criteria criteria) {
        Criteria c = criteria.normalized();

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE recon_exclusions SET name = :name, description = :description, " +
                                "enabled = :enabled, scan_types = :scan_types, source_cidr = :source_cidr, " +
                                "source_mac = :source_mac, target_cidr = :target_cidr, updated_at = NOW() " +
                                "WHERE id = :id")
                        .bind("id", id)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("enabled", enabled)
                        .bind("scan_types", c.scanTypes() == null ? null : String.join(",", c.scanTypes()))
                        .bind("source_cidr", c.sourceCidr())
                        .bind("source_mac", c.sourceMac())
                        .bind("target_cidr", c.targetCidr())
                        .execute()
        );
    }

    public void setEnabled(long id, boolean enabled) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE recon_exclusions SET enabled = :enabled, updated_at = NOW() " +
                                "WHERE id = :id")
                        .bind("id", id)
                        .bind("enabled", enabled)
                        .execute()
        );
    }

    public void delete(long id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM recon_exclusions WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public void recordMatches(Handle handle, long id, long count) {
        handle.createUpdate("UPDATE recon_exclusions SET match_count = match_count + :count, " +
                        "last_matched_at = NOW() WHERE id = :id")
                .bind("id", id)
                .bind("count", count)
                .execute();
    }

    public Map<ReconExclusionEntry, ReconExclusionMatcher> compileEnabled(Handle handle,
                                                                          UUID organizationId,
                                                                          UUID tenantId) {
        Map<ReconExclusionEntry, ReconExclusionMatcher> result = new LinkedHashMap<>();
        for (ReconExclusionEntry entry : findAllEnabled(handle, organizationId, tenantId)) {
            try {
                result.put(entry, ReconExclusionMatcher.create(entry));
            } catch (IllegalArgumentException e) {
                LOG.error("Recon exclusion [{}] of tenant [{}/{}] has invalid criteria and is ignored: {}",
                        entry.uuid(), organizationId, tenantId, e.getMessage());
            }
        }

        return result;
    }

    public Map<ReconExclusionEntry, ReconExclusionMatcher> compileEnabled(UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle -> compileEnabled(handle, organizationId, tenantId));
    }

    public record MatchedScan(UUID uuid, String type, String source, String target) { }

    public record Impact(int matchingScans, List<MatchedScan> scans) {
        public static Impact empty() {
            return new Impact(0, Collections.emptyList());
        }
    }

    public Impact calculateImpact(ReconExclusionMatcher matcher, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle -> matchExisting(handle, matcher, organizationId, tenantId));
    }

    public Impact applyToExistingScans(ReconExclusionMatcher matcher, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().inTransaction(handle -> {
            Impact impact = matchExisting(handle, matcher, organizationId, tenantId);

            for (ReconScanEntry scan : recon.findAllScansOfTenant(handle, organizationId, tenantId)) {
                if (matchesScan(handle, matcher, scan)) {
                    recon.deleteScanById(handle, scan.id());
                }
            }

            return impact;
        });
    }

    public boolean matchesScan(Handle handle, ReconExclusionMatcher matcher, ReconScanEntry scan) {
        if (!matcher.matchesType(scan.type()) || !matcher.matchesSource(scan.sourceAddress(), scan.sourceMac())) {
            return false;
        }

        if (!matcher.hasTargetCriteria()) {
            return true;
        }

        if (scan.targetAddress() != null) {
            return matcher.matchesTargets(scan.targetAddress(), null);
        }

        return matcher.matchesTargets(null, recon.findTargetAddresses(handle, scan.id(), TARGET_SAMPLE_SIZE));
    }

    private Impact matchExisting(Handle handle, ReconExclusionMatcher matcher, UUID organizationId, UUID tenantId) {
        List<MatchedScan> matched = Lists.newArrayList();
        for (ReconScanEntry scan : recon.findAllScansOfTenant(handle, organizationId, tenantId)) {
            if (!matchesScan(handle, matcher, scan)) {
                continue;
            }

            matched.add(new MatchedScan(
                    scan.uuid(),
                    scan.type(),
                    ReconDetector.sourceDescription(scan),
                    ReconDetector.targetDescription(scan)
            ));
        }

        if (matched.isEmpty()) {
            return Impact.empty();
        }

        matched.sort(Comparator.comparing(MatchedScan::source).thenComparing(MatchedScan::target));
        List<MatchedScan> sample = matched.size() > IMPACT_SAMPLE_SIZE
                ? matched.subList(0, IMPACT_SAMPLE_SIZE) : matched;

        return new Impact(matched.size(), Lists.newArrayList(sample));
    }

    public List<ReconExclusionEntry> findMatchingExclusions(ReconScanEntry scan,
                                                            Map<ReconExclusionEntry, ReconExclusionMatcher> compiled) {
        return nzyme.getDatabase().withHandle(handle -> {
            List<ReconExclusionEntry> result = Lists.newArrayList();
            for (Map.Entry<ReconExclusionEntry, ReconExclusionMatcher> e : compiled.entrySet()) {
                if (matchesScan(handle, e.getValue(), scan)) {
                    result.add(e.getKey());
                }
            }

            return result;
        });
    }

}
