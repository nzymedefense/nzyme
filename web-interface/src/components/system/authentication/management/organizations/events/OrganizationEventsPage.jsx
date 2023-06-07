import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import Routes from "../../../../../../util/ApiRoutes";
import OrganizationActions from "./actions/OrganizationActions";
import OrganizationEventSubscriptions from "./subscriptions/OrganizationEventSubscriptions";
import Events from "../../../../events/Events";
import OrganizationEvents from "./OrganizationEvents";

const authenticationMgmtService = new AuthenticationManagementService();

function OrganizationEventsPage() {

  const { organizationId } = useParams();

  const [organization, setOrganization] = useState(null);

  useEffect(() => {
    authenticationMgmtService.findOrganization(organizationId, setOrganization);
  }, [organizationId])

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
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication &amp; Authorization</a>
                </li>
                <li className="breadcrumb-item">Organizations</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                    {organization.name}
                  </a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Events &amp; Actions</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end"
               href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
              Back
            </a>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Events &amp; Actions of Organization &quot;{organization.name}&quot;</h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-8">
            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Organization Event Subscriptions</h3>

                    <p>
                      This table shows all system events that can be triggered within the organization, together with
                      their respective subscribed actions.
                    </p>

                    <OrganizationEventSubscriptions organizationId={organizationId} />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Organization Event Actions</h3>

                    <p>
                      Events, such as system notifications or detection alerts, within this organization, have the
                      ability to trigger the following actions. It is important to note that tenants of this
                      organization must be assigned access to individual event actions.
                    </p>

                    <OrganizationActions organizationId={organizationId} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>All Recorded Organization Events</h3>

                <p>
                  The table below displays all recorded events that can trigger actions within this organization. Please
                  note that detection alerts have additionally been organized in a separate section in the navigation panel
                  for easier and streamlined management.
                </p>

                <OrganizationEvents organizationId={organization.id} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default OrganizationEventsPage;