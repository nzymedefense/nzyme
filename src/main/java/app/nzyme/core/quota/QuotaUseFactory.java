package app.nzyme.core.quota;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.security.authentication.db.TenantEntry;

import java.util.UUID;

public class QuotaUseFactory {

    public static int organizationQuotaUse(NzymeNode nzyme, QuotaType type, UUID organizationUuid) {
        switch (type) {
            case TAPS:
                return nzyme.getTapManager()
                        .findAllTapsOfOrganization(organizationUuid).size();
            case TENANTS:
                return nzyme.getAuthenticationService()
                        .findAllTenantsOfOrganization(organizationUuid).size();
            case TENANT_USERS:
                int users = 0;
                for (TenantEntry tenant : nzyme.getAuthenticationService()
                        .findAllTenantsOfOrganization(organizationUuid)) {
                    users += nzyme.getAuthenticationService()
                            .findAllUsersOfTenant(organizationUuid, tenant.uuid(), Integer.MAX_VALUE, 0)
                            .size();
                }

                return users;
            case INTEGRATIONS_COT:
                return -1; // TODO
        };

        return -1;
    }

    public static int tenantQuotaUse(NzymeNode nzyme, QuotaType type, UUID organizationUuid, UUID tenantUuid) {
        if (type == QuotaType.TENANTS) {
            throw new IllegalArgumentException("Tenants have no tenant quota.");
        }

        switch (type) {
            case TAPS:
                return nzyme.getTapManager()
                        .findAllTapsOfTenant(organizationUuid, tenantUuid)
                        .size();
            case TENANT_USERS:
                return nzyme.getAuthenticationService()
                        .findAllUsersOfTenant(organizationUuid, tenantUuid, Integer.MAX_VALUE, 0)
                        .size();
            case INTEGRATIONS_COT:
                return -1; // TODO
        };

        return -1;
    }

}
