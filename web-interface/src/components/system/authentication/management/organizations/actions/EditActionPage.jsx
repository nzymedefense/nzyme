import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import EventActionsService from "../../../../../../services/EventActionsService";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import ActionFormProxy from "./forms/ActionFormProxy";
import {notify} from "react-notify-toast";

const authenticationMgmtService = new AuthenticationManagementService();
const eventActionsService = new EventActionsService();

function EditActionPage() {

  const { organizationId } = useParams();
  const { actionId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [action, setAction] = useState(null);

  const [redirect, setRedirect] = useState(false);

  useEffect(() => {
    authenticationMgmtService.findOrganization(organizationId, setOrganization);
    eventActionsService.findActionOfOrganization(organizationId, actionId, setAction)
  }, [organizationId, actionId])

  const onSubmit = function(name, description, subjectPrefix, receivers) {
    eventActionsService.updateEmailAction(action.id, name, description, subjectPrefix, receivers, function() {
      notify.show('Action updated.', 'success');
      setRedirect(true);
    })
  }

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ACTIONS.DETAILS(organization.id, action.id)} />
  }

  if (!organization || !action) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
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
                <li className="breadcrumb-item">Actions</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ACTIONS.DETAILS(organization.id, action.id)}>
                    {action.name}
                  </a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Edit</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end"
               href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ACTIONS.DETAILS(organization.id, action.id)}>
              Back
            </a>
          </div>

          <div className="col-md-12">
            <h1>Event Action &quot;{action.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Edit Action</h3>

                    <div className="mb-3">
                      <label htmlFor="actionType" className="form-label">Action Type</label>
                      <input type="text"
                             className="form-control"
                             id="actionType"
                             value={action.action_type_human_readable}
                             disabled={true} />
                    </div>

                    <ActionFormProxy action={action} onSubmit={onSubmit} type={action.action_type} buttonText="Update Action" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )
}

export default EditActionPage;