import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import {Navigate, useParams} from "react-router-dom";
import Routes from "../../../../../util/ApiRoutes";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import {notify} from "react-notify-toast";
import TapPermissionForm from "./TapPermissionForm";

const authenticationMgmtService = new AuthenticationManagementService();

function CreateTapPermissionPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationMgmtService.findOrganization(organizationId, setOrganization);
    authenticationMgmtService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  const onFormSubmitted = function (name, description) {
    authenticationMgmtService.createTapPermission(organizationId, tenantId, name, description, function() {
      notify.show('Tap created.', 'success');
      setRedirect(true);
    })
  }

  if (redirect) {
    return <Navigate to={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organizationId, tenantId)} />
  }

  if (!organization || !tenant) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">
                  <a href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication &amp; Authorization</a>
                </li>
                <li className="breadcrumb-item">Organizations</li>
                <li className="breadcrumb-item">
                  <a href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                    {organization.name}
                  </a>
                </li>
                <li className="breadcrumb-item">Tenants</li>
                <li className="breadcrumb-item">
                  <a href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                    {tenant.name}
                  </a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Create Tap</li>
              </ol>
            </nav>
          </div>

          <div className="col-2">
            <a className="btn btn-secondary float-end"
               href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
              Back
            </a>
          </div>

          <div className="col-12">
            <h1>Create Tap</h1>
          </div>

          <div className="row mt-3">
            <div className="col-xl-12 col-xxl-6">
              <div className="card">
                <div className="card-body">
                  <TapPermissionForm onClick={onFormSubmitted} submitText="Create Tap" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateTapPermissionPage;