import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import Routes from "../../../../../../../util/ApiRoutes";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../../../../shared/CardTitleWithControls";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import {notify} from "react-notify-toast";
import CotOutputForm from "./CotOutputForm";
import CotIntegrationService from "../../../../../../../services/integrations/CotIntegrationService";

const authenticationManagementService = new AuthenticationManagementService();
const cotIntegrationService = new CotIntegrationService();

export default function CreateCotOutputPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);

  const [redirect, setRedirect] = React.useState(false);

  const onFormSubmitted = (name, description, connectionType, tapLeafType, address, port, certificate, certificatePassphrase, onFailure) => {
    const formData = new FormData();

    formData.append("name", name);
    formData.append("connection_type", connectionType);
    formData.append("tap_leaf_type", tapLeafType);
    formData.append("address", address);
    formData.append("port", port);

    if (description) {
      formData.append("description", description);
    }

    if (certificate) {
      formData.append("certificate", certificate);
    }

    if (certificatePassphrase) {
      formData.append("certificate_passphrase", certificatePassphrase);
    }

    cotIntegrationService.createOutput(organizationId, tenantId, formData, () => {
          notify.show("Cursor on Target output created.", "success");
          setRedirect(true);
        }, onFailure)
  }

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  if (redirect) {
    return <Navigate to={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS_PAGE(organizationId, tenantId)} />
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
                <li className="breadcrumb-item">
                  <a href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication &amp; Authorization</a>
                </li>
                <li className="breadcrumb-item">Organizations</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                    {organization.name}
                  </a>
                </li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.TENANTS_PAGE(organization.id)}>
                    Tenants
                  </a>
                </li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                    {tenant.name}
                  </a>
                </li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS_PAGE(organization.id, tenant.id)}>
                    Integrations
                  </a>
                </li>
                <li className="breadcrumb-item">Cursor on Target</li>
                <li className="breadcrumb-item active" aria-current="page">Create Output</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-primary"
                 href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS_PAGE(organization.id, tenant.id)}>
                Back
              </a>
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>Create Cursor on Target Output</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Create Output" slim={true} />

                <CotOutputForm onSubmit={onFormSubmitted} submitText="Create Output" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  );

}