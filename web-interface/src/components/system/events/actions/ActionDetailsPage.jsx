import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import {notify} from "react-notify-toast";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ActionDetails from "../shared/ActionDetails";
import ActionDetailsProxy from "../shared/details/ActionDetailsProxy";
import EventActionsService from "../../../../services/EventActionsService";
import ApiRoutes from "../../../../util/ApiRoutes";

const eventActionsService = new EventActionsService();

function ActionDetailsPage() {

  const { actionId } = useParams();

  const [action, setAction] = useState(null);
  const [deleted, setDeleted] = useState(false);

  useEffect(() => {
    eventActionsService.findAction(actionId, setAction)
  }, [actionId])

  const onDelete = function() {
    if (!confirm("Really delete action?")) {
      return;
    }

    eventActionsService.deleteAction(action.id, function() {
      setDeleted(true);
      notify.show('Action deleted.', 'success');
    })
  }

  if (deleted) {
    return <Navigate to={ApiRoutes.SYSTEM.EVENTS.INDEX} />
  }

  if (!action) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-9">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.SYSTEM.EVENTS.INDEX}>Events &amp; Actions</a></li>
                <li className="breadcrumb-item">Actions</li>
                <li className="breadcrumb-item active" aria-current="page">{action.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary" href={ApiRoutes.SYSTEM.EVENTS.INDEX}>
                Back
              </a>{' '}
              <a className="btn btn-primary" href={ApiRoutes.SYSTEM.EVENTS.ACTIONS.EDIT(action.id)}>
                Edit Action
              </a>
            </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Event Action &quot;{action.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Description</h3>

                    <p className="mb-0">
                      {action.description}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Details</h3>

                    <ActionDetails action={action} />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Configuration</h3>

                    <ActionDetailsProxy action={action} />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Delete Action</h3>

                    <p>
                      <strong>Note:</strong> You can only delete actions that are not currently subscribed to any events
                      like system notifications or detection alerts.
                    </p>

                    <button type="button" className="btn btn-danger" onClick={onDelete}>
                      Delete Action
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ActionDetailsPage;