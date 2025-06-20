import React, {useEffect, useState} from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import BluetoothService from "../../../services/BluetoothService";
import BluetoothMonitoringRulesTable from "./BluetoothMonitoringRulesTable";
import ApiRoutes from "../../../util/ApiRoutes";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";

const bluetoothService = new BluetoothService();

export default function BluetoothMonitoringPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [rules, setRules] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 25;

  useEffect(() => {
    bluetoothService.findAllRules(setRules, perPage, (page-1)*perPage)
  }, [organizationId, tenantId, page]);

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <h1>Bluetooth Monitoring</h1>
          </div>

          <div className="col-md-2 text-end">
            <a href="https://go.nzyme.org/bluetooth-monitoring" className="btn btn-secondary">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Monitoring Rules" />

                <BluetoothMonitoringRulesTable page={page} setPage={setPage} rules={rules} />

                <div className="mt-2">
                  <a href={ApiRoutes.BLUETOOTH.MONITORING.RULES.CREATE(organizationId, tenantId)}
                     className="btn btn-sm btn-secondary">
                    Create Bluetooth Monitoring Rule
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}