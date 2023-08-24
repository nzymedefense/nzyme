import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../services/AuthenticationManagementService";
import EventActionsService from "../../../services/EventActionsService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ApiRoutes from "../../../util/ApiRoutes";
import EventSubscriptionsTable from "../../system/events/shared/subscriptions/EventSubscriptionsTable";
import EventSubscriptionActionSelector from "../../system/events/shared/subscriptions/EventSubscriptionActionSelector";
import {notify} from "react-notify-toast";

const authenticationMgmtService = new AuthenticationManagementService();
const eventActionsService = new EventActionsService();

function AlertSubscriptionDetailsPage() {

  const { organizationId } = useParams();
  const { detectionName } = useParams();

  const [organization, setOrganization] = useState(null);

  const [detectionType, setDetectionType] = useState(null);
  const [actions, setActions] = useState(null);

  const [revision, setRevision] = useState(0);
  const [subscriptionError, setSubscriptionError] = useState(null);

  const onActionSelect = function(actionId) {
    eventActionsService.subscribeActionToDetectionEvent(detectionType.name, actionId, organizationId,function() {
      notify.show("Subscribed action to detection event.", "success");
      setRevision(revision+1);
    }, function(error) {
      setSubscriptionError(error.response.data.message);
    });
  }

  const onUnsubscribeClick = function(subscriptionId) {
    if (!confirm("Really unsubscribe action from detection event?")) {
      return;
    }

    eventActionsService.unsubscribeActionFromDetectionEvent(detectionType.name, subscriptionId, function() {
      notify.show("Unsubscribed action from event.", "success");
      setRevision(revision+1);
    })
  }

  useEffect(() => {
    setActions(null);

    authenticationMgmtService.findOrganization(organizationId, setOrganization);
    eventActionsService.findDetectionType(detectionName, setDetectionType, organizationId);
    eventActionsService.findAllActionsOfOrganization(organizationId, setActions, 9999999, 0);
  }, [organizationId, detectionName, revision])

  if (!organization || !detectionType || !actions) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.ALERTS.INDEX}>Alerts</a>
                </li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.ALERTS.SUBSCRIPTIONS.INDEX}>Subscriptions</a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">
                  {detectionType.title}
                </li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={ApiRoutes.ALERTS.SUBSCRIPTIONS.INDEX}>Back</a>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Subscriptions of Detection Event &quot;{detectionType.title}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Subscribed Actions</h3>

                <EventSubscriptionsTable organizationId={organizationId}
                                         subscriptions={detectionType.subscriptions}
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

export default AlertSubscriptionDetailsPage;