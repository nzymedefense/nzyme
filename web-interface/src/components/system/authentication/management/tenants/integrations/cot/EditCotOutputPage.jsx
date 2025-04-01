import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import CotIntegrationService from "../../../../../../../services/integrations/CotIntegrationService";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import Routes from "../../../../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../../../../shared/CardTitleWithControls";
import CotOutputForm from "./CotOutputForm";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();
const cotIntegrationService = new CotIntegrationService();

export default function EditCotOutputPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();
  const { outputId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [output, setOutput] = useState(null);

  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);

    setOutput(null);
    cotIntegrationService.findOutput(setOutput, organizationId, tenantId, outputId);
  }, [organizationId, tenantId, outputId])

  const onFormSubmitted = (name, description, connectionType, tapLeafType, address, port, certificate, onFailure) => {
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

    cotIntegrationService.editOutput(organizationId, tenantId, outputId, formData, () => {
      notify.show("Cursor on Target output updated.", "success");
      setRedirect(true);
    }, onFailure)
  }

  if (!organization || !tenant || !output) {
    return <LoadingSpinner />
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.DETAILS(organizationId, tenantId, outputId)} />
  }

  if (!organization || !tenant || !output) {
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
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.DETAILS(organizationId, tenantId, outputId)}>
                    {output.name}
                  </a>
                </li>
                <li className="breadcrumb-item active">Edit</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary"
                 href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.DETAILS(organizationId, tenantId, outputId)}>
                Back
              </a>&nbsp;
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>Edit Cursor on Target Output &quot;{output.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Edit Output" slim={true} />

                <CotOutputForm onSubmit={onFormSubmitted}
                               name={output.name}
                               description={output.description}
                               connectionType={output.connection_type}
                               tapLeafType={output.leaf_type_tap}
                               address={output.address}
                               port={output.port}
                               submitText="Edit Output" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  );

}