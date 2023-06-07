import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import EventActionsService from "../../../../services/EventActionsService";
import ApiRoutes from "../../../../util/ApiRoutes";
import EventSubscriptionsTable from "../shared/subscriptions/EventSubscriptionsTable";
import EventSubscriptionActionSelector from "../shared/subscriptions/EventSubscriptionActionSelector";
import {notify} from "react-notify-toast";

const eventActionsService = new EventActionsService();

function EventSubscriptionDetailsPage() {

  const { eventTypeName } = useParams();

  const [eventType, setEventType] = useState(null);
  const [actions, setActions] = useState(null);

  const [revision, setRevision] = useState(0);
  const [subscriptionError, setSubscriptionError] = useState(null);

  useEffect(() => {
    setActions(null);

    eventActionsService.findSystemEventType(eventTypeName, setEventType);
    eventActionsService.findAllActions(setActions, 9999999, 0);
  }, [eventTypeName, revision])

  const onActionSelect = function(actionId) {
    eventActionsService.subscribeActionToEvent(eventType.id, actionId, null,function() {
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

  if (!eventType || !actions) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-9">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.SYSTEM.EVENTS.INDEX}>Events &amp; Actions</a></li>
                <li className="breadcrumb-item">Subscriptions</li>
                <li className="breadcrumb-item active" aria-current="page">{eventType.category_name} - {eventType.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary" href={ApiRoutes.SYSTEM.EVENTS.INDEX}>
                Back
              </a>
            </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Subscriptions of Event &quot;{eventType.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Subscribed Actions</h3>

                <EventSubscriptionsTable subscriptions={eventType.subscriptions}
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

export default EventSubscriptionDetailsPage;