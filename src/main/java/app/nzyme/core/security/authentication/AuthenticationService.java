package app.nzyme.core.security.authentication;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class AuthenticationService {

    private static final Logger LOG = LogManager.getLogger(AuthenticationService.class);

    public final NzymeNode nzyme;

    public AuthenticationService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        seedDatabase();
    }

    private void seedDatabase() {
        long orgCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM auth_organizations")
                        .mapTo(Long.class)
                        .one()
        );

        if (orgCount > 0) {
            return;
        }

        LOG.info("Creating default organization and tenant.");

        DateTime now = DateTime.now();
        OrganizationEntry organization = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO auth_organizations(name, description, created_at, updated_at) " +
                                "VALUES(:name, :description, :created_at, :updated_at) RETURNING *")
                        .bind("name", "Default Organization")
                        .bind("description", "The nzyme default organization.")
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .mapTo(OrganizationEntry.class)
                        .one()
        );

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO auth_tenants(organization_id, name, description, created_at, updated_at) " +
                                "VALUES(:organization_id, :name, :description, :created_at, :updated_at)")
                        .bind("organization_id", organization.id())
                        .bind("name", "Default Tenant")
                        .bind("description", "The nzyme default tenant.")
                        .bind("created_at", now)
                        .bind("updated_at", now)
                        .execute()
        );

        LOG.info(organization);
    }

}
