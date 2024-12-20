import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import EventActionsService from "../../../../../../../services/EventActionsService";
import EventSubscriptionsTable from "../../../../../events/shared/subscriptions/EventSubscriptionsTable";
import EventSubscriptionActionSelector
  from "../../../../../events/shared/subscriptions/EventSubscriptionActionSelector";
import {notify} from "react-notify-toast";

const authenticationMgmtService = new AuthenticationManagementService();
const eventActionsService = new EventActionsService();

function OrganizationEventSubscriptionDetailsPage() {

  const { organizationId } = useParams();
  const { eventTypeName } = useParams();

  const [organization, setOrganization] = useState(null);

  const [eventType, setEventType] = useState(null);
  const [actions, setActions] = useState(null);

  const [revision, setRevision] = useState(0);
  const [subscriptionError, setSubscriptionError] = useState(null);

  useEffect(() => {
    setActions(null);

    authenticationMgmtService.findOrganization(organizationId, setOrganization);
    eventActionsService.findSystemEventType(eventTypeName, setEventType, organizationId);
    eventActionsService.findAllActionsOfOrganization(organizationId, setActions, 9999999, 0);
  }, [organizationId, eventTypeName, revision])

  const onActionSelect = function(actionId) {
    eventActionsService.subscribeActionToEvent(eventType.id, actionId, organizationId,function() {
      notify.show("Subscribed action to event.", "success");
      setRevision(revision+1);
    }, function(error) {
      setSubscriptionError(error.response.data.message);
    });
  }

  const onUnsubscribeClick = function(subscriptionId) {
    if (!confirm("Really unsubscribe action from event?")) {
      return;
    }

    eventActionsService.unsubscribeActionFromEvent(eventType.id, subscriptionId, function() {
      notify.show("Unsubscribed action from event.", "success");
      setRevision(revision+1);
    })
  }

  if (!organization || !eventType || !actions) {
    return <LoadingSpinner />
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
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS_PAGE(organization.id)}>
                    Events &amp; Actions
                  </a>
                </li>
                <li className="breadcrumb-item">Subscriptions</li>
                <li className="breadcrumb-item active" aria-current="page">
                  {eventType.category_name} - {eventType.name}
                </li>
              </ol>
            </nav>
          </div>

          <div className="col-2">
            <a className="btn btn-primary float-end"
               href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS_PAGE(organization.id)}>
              Back
            </a>
          </div>
        </div>

        <div className="row">
          <div className="col-12">
            <h1>Subscriptions of Organization Event &quot;{eventType.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Subscribed Actions</h3>

                <EventSubscriptionsTable organizationId={organizationId}
                                         subscriptions={eventType.subscriptions}
                                         onUnsubscribeClick={onUnsubscribeClick} />

                <h4 className="mt-4 mb-0">Subscribe Action</h4>
                <EventSubscriptionActionSelector onSubmit={onActionSelect}
                                                 actions={actions.actions}
                                                 subscriptionError={subscriptionError} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default OrganizationEventSubscriptionDetailsPage;