import React from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import UavAlertSettings from "./UavAlertSettings";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";

export default function UavMonitoringPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <h1>Unmanned Aerial Vehicle (UAV) Monitoring</h1>
          </div>

          <div className="col-md-2 text-end">
            <a href="https://go.nzyme.org/uav-monitoring" className="btn btn-secondary">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Alert Settings" />

                <UavAlertSettings organizationId={organizationId} tenantId={tenantId} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
 )

}