package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.ethernet.EthernetRegistryKeys;
import app.nzyme.core.rest.requests.RetentionTimeConfigurationUpdateRequest;
import app.nzyme.core.rest.responses.bluetooth.BluetoothRegistryKeys;
import app.nzyme.core.rest.responses.system.DatabaseSummaryResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryValueType;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/system/database")
@RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class DatabaseResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response summary() {
        DatabaseImpl db = (DatabaseImpl) nzyme.getDatabase();
        long totalSize = db.getTotalSize();
        long ethernetSize = db.getTableSize("dns_log")
                + db.getTableSize("dns_entropy_log")
                + db.getTableSize("dns_pairs")
                + db.getTableSize("dns_statistics");

        long dot11Size = db.getTableSize("dot11_bssids")
                + db.getTableSize("dot11_channels")
                + db.getTableSize("dot11_fingerprints")
                + db.getTableSize("dot11_ssids")
                + db.getTableSize("dot11_infrastructure_types")
                + db.getTableSize("dot11_bssid_clients")
                + db.getTableSize("dot11_rates")
                + db.getTableSize("dot11_clients")
                + db.getTableSize("dot11_client_probereq_ssids")
                + db.getTableSize("dot11_channel_histograms")
                + db.getTableSize("dot11_disco_activity")
                + db.getTableSize("dot11_disco_activity_receivers");

        int dot11RetentionTime = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key())
                .orElse(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );

        int ethernetL4RetentionTime = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.key())
                .orElse(EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );

        int ethernetDnsRetentionTime = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.key())
                .orElse(EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );

        int ethernetArpRetentionTime = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(EthernetRegistryKeys.ARP_RETENTION_TIME_DAYS.key())
                .orElse(EthernetRegistryKeys.ARP_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );

        ConfigurationEntryResponse dot11RetentionTimeConfig = ConfigurationEntryResponse.create(
                Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key(),
                "Retention Time (in days)",
                dot11RetentionTime,
                ConfigurationEntryValueType.NUMBER,
                Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.defaultValue().orElse(null),
                Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.requiresRestart(),
                Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.constraints().orElse(Lists.newArrayList()),
                "retention-time"
        );

        ConfigurationEntryResponse bluetoothRetentionTimeConfig = ConfigurationEntryResponse.create(
                BluetoothRegistryKeys.BLUETOOTH_RETENTION_TIME_DAYS.key(),
                "Retention Time (in days)",
                dot11RetentionTime,
                ConfigurationEntryValueType.NUMBER,
                BluetoothRegistryKeys.BLUETOOTH_RETENTION_TIME_DAYS.defaultValue().orElse(null),
                BluetoothRegistryKeys.BLUETOOTH_RETENTION_TIME_DAYS.requiresRestart(),
                BluetoothRegistryKeys.BLUETOOTH_RETENTION_TIME_DAYS.constraints().orElse(Lists.newArrayList()),
                "retention-time"
        );

        ConfigurationEntryResponse ethernetL4RetentionTimeConfig = ConfigurationEntryResponse.create(
                EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.key(),
                "Retention Time (in days)",
                ethernetL4RetentionTime,
                ConfigurationEntryValueType.NUMBER,
                EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.defaultValue().orElse(null),
                EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.requiresRestart(),
                EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.constraints().orElse(Lists.newArrayList()),
                "retention-time"
        );

        ConfigurationEntryResponse ethernetDnsRetentionTimeConfig = ConfigurationEntryResponse.create(
                EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.key(),
                "Retention Time (in days)",
                ethernetDnsRetentionTime,
                ConfigurationEntryValueType.NUMBER,
                EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.defaultValue().orElse(null),
                EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.requiresRestart(),
                EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.constraints().orElse(Lists.newArrayList()),
                "retention-time"
        );

        ConfigurationEntryResponse ethernetArpRetentionTimeConfig = ConfigurationEntryResponse.create(
                EthernetRegistryKeys.ARP_RETENTION_TIME_DAYS.key(),
                "Retention Time (in days)",
                ethernetArpRetentionTime,
                ConfigurationEntryValueType.NUMBER,
                EthernetRegistryKeys.ARP_RETENTION_TIME_DAYS.defaultValue().orElse(null),
                EthernetRegistryKeys.ARP_RETENTION_TIME_DAYS.requiresRestart(),
                EthernetRegistryKeys.ARP_RETENTION_TIME_DAYS.constraints().orElse(Lists.newArrayList()),
                "retention-time"
        );

        return Response.ok(DatabaseSummaryResponse.create(
                totalSize,
                ethernetSize,
                dot11Size,
                dot11RetentionTimeConfig,
                bluetoothRetentionTimeConfig,
                ethernetL4RetentionTimeConfig,
                ethernetDnsRetentionTimeConfig,
                ethernetArpRetentionTimeConfig
        )).build();
    }

    @PUT
    @Path("/retention")
    public Response updateRetentionTimeConfiguration(@Valid RetentionTimeConfigurationUpdateRequest ur) {
        for (Map.Entry<String, Object> c : ur.change().entrySet()) {
            switch (c.getKey()) {
                case "dot11_retention_time_days":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS, c)) {
                        return Response.status(422).build();
                    }
                    break;
                case "bluetooth_retention_time_days":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(BluetoothRegistryKeys.BLUETOOTH_RETENTION_TIME_DAYS, c)) {
                        return Response.status(422).build();
                    }
                    break;
                case "ethernet_l4_retention_time_days":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(EthernetRegistryKeys.L4_RETENTION_TIME_DAYS, c)) {
                        return Response.status(422).build();
                    }
                    break;
                case "ethernet_dns_retention_time_days":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS, c)) {
                        return Response.status(422).build();
                    }
                    break;
                case "ethernet_arp_retention_time_days":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(EthernetRegistryKeys.ARP_RETENTION_TIME_DAYS, c)) {
                        return Response.status(422).build();
                    }
                    break;
                default:
                    return Response.status(422).build();
            }

            nzyme.getDatabaseCoreRegistry().setValue(c.getKey(), c.getValue().toString());
        }

        return Response.ok().build();
    }

}
