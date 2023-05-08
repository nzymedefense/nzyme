import React, {useEffect, useState} from "react";
import Routes from "../../../../../util/ApiRoutes";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import {Navigate, useParams} from "react-router-dom";
import {notify} from "react-notify-toast";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import TenantForm from "./TenantForm";

const authenticationMgmtService = new AuthenticationManagementService();

function CreateTenantPage() {

  const { organizationId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationMgmtService.findOrganization(organizationId, setOrganization);
  }, [organizationId])

  const onFormSubmitted = function (name, description) {
    authenticationMgmtService.createTenantOfOrganization(organizationId, name, description, function() {
      notify.show('Tenant created.', 'success');
      setRedirect(true);
    })
  }

  if (redirect) {
    return <Navigate to={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organizationId)} />
  }

  if (!organization) {
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
                <li className="breadcrumb-item">
                  <a href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>{organization.name}</a>
                </li>
                <li className="breadcrumb-item">Tenants</li>
                <li className="breadcrumb-item active" aria-current="page">Create</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-secondary float-end"
               href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
              Back
            </a>
          </div>

          <div className="col-md-12">
            <h1>Create Tenant</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <TenantForm onClick={onFormSubmitted} submitText="Create Tenant" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateTenantPage;