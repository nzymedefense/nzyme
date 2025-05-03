import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../util/ApiRoutes";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import SelectedOrganizationAndTenant from "../../shared/SelectedOrganizationAndTenant";
import BluetoothMonitoringRuleForm from "./BluetoothMonitoringRuleForm";

const authenticationManagementService = new AuthenticationManagementService();

export default function CreateBluetoothMonitoringRulePage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  const onFormSubmitted = (name, onFailure) => {
  }

  if (!organization || !tenant) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-9">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item">Bluetooth</li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.BLUETOOTH.MONITORING.INDEX}>Monitoring</a>
              </li>
              <li className="breadcrumb-item">Rules</li>
              <li className="breadcrumb-item active" aria-current="page">Create Rule</li>
            </ol>
          </nav>
        </div>

        <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.BLUETOOTH.MONITORING.INDEX}>
                Back
              </a>
            </span>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <h1>Create Bluetooth Monitoring Rule</h1>
        </div>
      </div>

      <SelectedOrganizationAndTenant
        organizationId={organizationId}
        tenantId={tenantId}
        noEdit={true} />

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Create Rule" slim={true} />

              <BluetoothMonitoringRuleForm onSubmit={onFormSubmitted} submitText="Create Output" />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}