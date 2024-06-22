package app.nzyme.core.tables.bluetooth;

import app.nzyme.core.rest.resources.taps.reports.tables.bluetooth.BluetoothDeviceReport;
import app.nzyme.core.rest.resources.taps.reports.tables.bluetooth.BluetoothDevicesReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.tables.ssh.SSHTable;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class BluetoothTable implements DataTable {

    private static final Logger LOG = LogManager.getLogger(SSHTable.class);

    private final TablesService tablesService;

    private final Timer totalReportTimer;
    private final ObjectMapper om;

    public BluetoothTable(TablesService tablesService) {
        this.tablesService = tablesService;
        this.om = new ObjectMapper();

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.BLUETOOTH_TOTAL_REPORT_PROCESSING_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, BluetoothDevicesReport report) {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            try(Timer.Context ignored = totalReportTimer.time()) {
                writeDevices(handle, tapUuid, report.devices());
            }
        });
    }

    private void writeDevices(Handle handle, UUID tapUuid, List<BluetoothDeviceReport> devices) {
        PreparedBatch batch = handle.prepareBatch("INSERT INTO bluetooth_devices(uuid, tap_uuid, mac, alias, " +
                "device, transport, name, rssi, company_id, class_number, appearance, modalias, tx_power, " +
                "manufacturer_data, uuids, service_data, last_seen, created_at) VALUES(:uuid, :tap_uuid, :mac, " +
                ":alias, :device, :transport, :name, :rssi, :company_id, :class_number, :appearance, :modalias, " +
                ":tx_power, :manufacturer_data, :uuids, :service_data, :last_seen, NOW())");

        for (BluetoothDeviceReport device : devices) {
            String uuids = null;
            String serviceData = null;
            try {
                uuids = om.writeValueAsString(device.uuids());
                serviceData = om.writeValueAsString(device.serviceData());
            } catch (JsonProcessingException e) {
                LOG.warn("Could not serialize Bluetooth device data. Skipping attributes.", e);
            }

            batch
                    .bind("uuid", UUID.randomUUID())
                    .bind("tap_uuid", tapUuid)
                    .bind("mac", device.mac())
                    .bind("alias", device.alias())
                    .bind("device", device.device())
                    .bind("transport", device.transport())
                    .bind("name", device.name())
                    .bind("rssi", device.rssi())
                    .bind("company_id", device.companyId())
                    .bind("class_number", device.classNumber())
                    .bind("appearance", device.appearance())
                    .bind("modalias", device.modalias())
                    .bind("tx_power", device.txPower())
                    .bind("manufacturer_data", device.manufacturerData())
                    .bind("uuids", uuids)
                    .bind("service_data", serviceData)
                    .bind("last_seen", device.lastSeen())
                    .add();
        }

        batch.execute();
    }

    @Override
    public void retentionClean() {
        throw new RuntimeException("Not Implemented.");
    }

}
