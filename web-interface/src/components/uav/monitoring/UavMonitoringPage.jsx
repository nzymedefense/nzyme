import React, {useState} from "react";
import OrganizationAndTenantSelector from "../../shared/OrganizationAndTenantSelector";
import SelectedOrganizationAndTenant from "../../shared/SelectedOrganizationAndTenant";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import UavAlertSettings from "./UavAlertSettings";

export default function UavMonitoringPage() {

  const [organizationId, setOrganizationId] = useState(null);
  const [tenantId, setTenantId] = useState(null);
  const [tenantSelected, setTenantSelected] = useState(false);

  const onOrganizationChange = (uuid) => {
    setOrganizationId(uuid);
  }

  const onTenantChange = (uuid) => {
    setTenantId(uuid);

    if (uuid) {
      setTenantSelected(true);
    }
  }

  const resetTenantAndOrganization = () => {
    setOrganizationId(null);
    setTenantId(null);
  }

  if (!organizationId || !tenantId) {
    return <OrganizationAndTenantSelector onOrganizationChange={onOrganizationChange}
                                          onTenantChange={onTenantChange}
                                          autoSelectCompleted={tenantSelected} />
  }

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

        <SelectedOrganizationAndTenant
            organizationId={organizationId}
            tenantId={tenantId}
            onReset={resetTenantAndOrganization} />

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