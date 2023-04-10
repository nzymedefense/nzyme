import React, {useState} from "react";
import Routes from "../../../../../util/ApiRoutes";
import OrganizationForm from "./OrganizationForm";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import {Navigate} from "react-router-dom";
import {notify} from "react-notify-toast";

const authenticationMgmtService = new AuthenticationManagementService();

function CreateOrganizationPage() {

  const [redirect, setRedirect] = useState(false);

  const onFormSubmitted = function (name, description) {
    authenticationMgmtService.createOrganization(name, description, function() {
      notify.show('Organization created.', 'success');
      setRedirect(true);
    })
  }

  if (redirect) {
    return <Navigate to={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX} />
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
                <li className="breadcrumb-item active" aria-current="page">Create Organization</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-secondary float-end" href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Back</a>
          </div>

          <div className="col-md-12">
            <h1>Create Organization</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <OrganizationForm onClick={onFormSubmitted} submitText="Create Organization" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CreateOrganizationPage;