import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import Routes from "../../../../../util/ApiRoutes";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import UserForm from "./UserForm";
import {notify} from "react-notify-toast";

const authenticationMgmtService = new AuthenticationManagementService();

function CreateUserPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationMgmtService.findOrganization(organizationId, setOrganization);
    authenticationMgmtService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  const onFormSubmitted = function (email, password, name) {
    authenticationMgmtService.createUserOfTenant(organizationId, tenantId, email, password, name, function() {
      notify.show('User created.', 'success');
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
          <div className="col-md-10">
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
                <li className="breadcrumb-item active" aria-current="page">Create User</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-secondary float-end"
               href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
              Back
            </a>
          </div>

          <div className="col-md-12">
            <h1>Create User</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <UserForm onClick={onFormSubmitted} submitText="Create User" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateUserPage;