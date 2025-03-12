package app.nzyme.core.quota;

import app.nzyme.core.NzymeNode;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class QuotaService {

    private final NzymeNode nzyme;

    public QuotaService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public Optional<Integer> getOrganizationQuota(UUID organizationId, QuotaType quotaType) {
        return nzyme.getDatabaseCoreRegistry()
                .getValue(constructRegistryKey(quotaType), organizationId)
                .map(Integer::parseInt);
    }

    public Map<QuotaType, Optional<Integer>> getAllOrganizationQuotas(UUID organizationId) {
        Map<QuotaType, Optional<Integer>> quotas = Maps.newTreeMap();

        for (QuotaType quotaType : Arrays.stream(QuotaType.values()).sorted().toList()) {
            quotas.put(quotaType, getOrganizationQuota(organizationId, quotaType));
        }

        return quotas;
    }

    public Optional<Integer> getTenantQuota(UUID organizationId, UUID tenantId, QuotaType quotaType) {
        return nzyme.getDatabaseCoreRegistry()
                .getValue(constructRegistryKey(quotaType), organizationId, tenantId)
                .map(Integer::parseInt);
    }

    public Map<QuotaType, Optional<Integer>> getAllTenantQuotas(UUID organizationId, UUID tenantId) {
        Map<QuotaType, Optional<Integer>> quotas = Maps.newTreeMap();

        for (QuotaType quotaType : Arrays.stream(QuotaType.values()).sorted().toList()) {
            quotas.put(quotaType, getTenantQuota(organizationId, tenantId, quotaType));
        }

        return quotas;
    }


    public void setOrganizationQuota(UUID organizationId, QuotaType quotaType, int quota) {
        nzyme.getDatabaseCoreRegistry()
                .setValue(constructRegistryKey(quotaType), String.valueOf(quota), organizationId);
    }

    public void setTenantQuota(UUID organizationId, UUID tenantId, QuotaType quotaType, int quota) {

        nzyme.getDatabaseCoreRegistry()
                .setValue(constructRegistryKey(quotaType), String.valueOf(quota), organizationId, tenantId);
    }

    private String constructRegistryKey(QuotaType quotaType) {
        return "quota_" + quotaType.name().toLowerCase();
    }

}
