import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import Routes from "../../../../../../util/ApiRoutes";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import EventActionsService from "../../../../../../services/EventActionsService";

const authenticationMgmtService = new AuthenticationManagementService();
const eventActionsService = new EventActionsService();

function ActionDetailsPage() {

  const { organizationId } = useParams();
  const { actionId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [action, setAction] = useState(null);

  useEffect(() => {
    authenticationMgmtService.findOrganization(organizationId, setOrganization);
    eventActionsService.findActionOfOrganization(organizationId, actionId, setAction)
  }, [organizationId, actionId])

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
                <li className="breadcrumb-item active" aria-current="page">{action.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end"
               href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organizationId)}>
              Back
            </a>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ActionDetailsPage;