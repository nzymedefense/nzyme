import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import EventActionsService from "../../../../services/EventActionsService";
import ApiRoutes from "../../../../util/ApiRoutes";
import EventSubscriptionsTable from "./EventSubscriptionsTable";
import EventSubscriptionActionSelector from "./EventSubscriptionActionSelector";

const eventActionsService = new EventActionsService();

function EventSubscriptionDetailsPage() {

  const { eventTypeName } = useParams();

  const [eventType, setEventType] = useState(null);
  const [actions, setActions] = useState(null);

  useEffect(() => {
    eventActionsService.findSystemEventType(eventTypeName, setEventType);
    eventActionsService.findAllActions(setActions, 9999999, 0);
  }, [eventTypeName])

  const onActionSelect = function(actionId) {
    console.log(actionId);
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

                <EventSubscriptionsTable subscriptions={eventType.subscriptions} />
                <EventSubscriptionActionSelector onSubmit={onActionSelect} actions={actions.actions} />
              </div>
            </div>
          </div>
        </div>


      </React.Fragment>
  )

}

export default EventSubscriptionDetailsPage;