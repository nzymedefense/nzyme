import React from "react";
import AlertSubscriptionsTable from "./AlertSubscriptionsTable";

function AlertSubscriptionsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <h1>Alert Subscriptions</h1>
          </div>

          <div className="col-md-2">
            <a href="https://go.nzyme.org/detection-alerts-subscriptions" className="btn btn-secondary float-end">
              Help
            </a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Alert Subscriptions</h3>

                <AlertSubscriptionsTable />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default AlertSubscriptionsPage;