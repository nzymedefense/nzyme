import React from "react";
import Events from "./Events";
import EventSubscriptions from "./subscriptions/EventSubscriptions";
import Actions from "./actions/Actions";

function EventsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Events &amp; Actions</h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="row mt-3">
              <div className="col-md-8">
                <div className="card">
                  <div className="card-body">
                    <h3>Actions</h3>

                    <p>
                      Events, such as system notifications or detection alerts have the ability to trigger the following
                      actions. This super administrator view of event actions does not include organization-specific
                      actions, which are instead managed on the respective organization management pages.
                    </p>

                    <Actions />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-8">
                <div className="card">
                  <div className="card-body">
                    <h3>Event Subscriptions</h3>

                    <p>
                      This super administrator view of event subscriptions does not include organization-specific
                      event types, which are instead managed on the respective organization management pages.
                    </p>

                    <EventSubscriptions />
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
                <h3>Recorded Events</h3>

                <p>
                  The table below displays all recorded events that can trigger actions within nzyme. Please note
                  that detection alerts have additionally been organized in a separate section in the navigation panel
                  for easier and streamlined management.
                </p>

                <Events />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default EventsPage;