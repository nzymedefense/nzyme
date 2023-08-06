import React from "react";
import AlertsTable from "./AlertsTable";


function AlertsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <h1>Monitored Networks</h1>
          </div>

          <div className="col-md-2">
            <a href="https://go.nzyme.org/wifi-network-monitoring" className="btn btn-secondary float-end">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>All Alerts</h3>

                <AlertsTable />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default AlertsPage;