import React from "react";
import EventsTable from "./EventsTable";

function EventsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Events &amp; Triggers</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Recorded Events</h3>

                <p>
                  The table below displays all recorded events that can trigger actions within nzyme. Please note
                  that detection alerts have been organized in a separate section in the navigation panel for easier
                  and streamlined management.
                </p>

                <EventsTable />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default EventsPage;