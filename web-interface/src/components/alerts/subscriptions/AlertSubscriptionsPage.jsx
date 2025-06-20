import React from "react";
import AlertSubscriptionsTable from "./AlertSubscriptionsTable";
import WildcardAlertSubscriptions from "./WildcardAlertSubscriptions";

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

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Wildcard Alert Subscriptions</h3>

                <p className="text-muted">
                  You can use <i>Wildcard Alert Subscriptions</i> to subscribe an action to all detection alert
                  types. By using this method, you don&apos;t have to manually assign the action to all alert types.
                </p>

                <WildcardAlertSubscriptions />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default AlertSubscriptionsPage;