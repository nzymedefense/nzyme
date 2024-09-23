import React, {useEffect, useState} from "react";

import {notify} from "react-notify-toast";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import {Navigate, useParams} from "react-router-dom";
import CreateUserForm from "../shared/CreateUserForm";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";

const authenticationMgmtService = new AuthenticationManagementService();

function CreateOrganizationAdministratorPage() {

  const { organizationId } = useParams();

  const [organization, setOrganization] = useState(null);

  const [errorMessage, setErrorMessage] = useState(null);
  const [redirect, setRedirect] = useState(false);

  const onFormSubmitted = function (email, password, name, callback) {
    authenticationMgmtService.createOrganizationAdministrator(organizationId, email, password, name, function() {
      // Success.
      notify.show('Organization administrator created.', 'success');
      setRedirect(true);
      callback();
    }, function (error) {
      // Error.
      setErrorMessage(error.response.data.message);
      callback();
    })
  }

  useEffect(() => {
    authenticationMgmtService.findOrganization(organizationId, setOrganization);
  }, [organizationId])

  if (!organization) {
    return <LoadingSpinner />
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication &amp; Authorization</a>
                </li>
                <li className="breadcrumb-item">Organizations</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                    {organization.name}
                  </a>
                </li>
                <li className="breadcrumb-item">Organization Administrators</li>
                <li className="breadcrumb-item active" aria-current="page">Create</li>
              </ol>
            </nav>
          </div>

          <div className="col-2">
            <a className="btn btn-secondary float-end"
               href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
              Back
            </a>
          </div>

          <div className="col-12">
            <h1>Create Organization Administrator</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CreateUserForm onClick={onFormSubmitted} errorMessage={errorMessage} submitText="Create Organization Administrator" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateOrganizationAdministratorPage;