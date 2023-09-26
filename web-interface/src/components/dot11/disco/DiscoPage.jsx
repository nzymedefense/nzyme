import React from "react";
import DiscoHistogram from "./DiscoHistogram";

function DiscoPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <h1>Deauthentication Activity</h1>
          </div>

          <div className="col-md-2">
            <a href="https://go.nzyme.org/disco" className="btn btn-secondary float-end">Help</a>
          </div>
        </div>

        <div className="alert alert-warning">
          This is a preview of deauthentication analysis with extremely limited functionality.
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Deauthentication Frames Observed</h3>

                <DiscoHistogram discoType="deauthentication" minutes={24*60} />
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Disassociation Frames Observed</h3>

                <DiscoHistogram discoType="disassociation" minutes={24*60} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default DiscoPage;