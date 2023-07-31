import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import Dot11MonitoredNetworksTable from "./Dot11MonitoredNetworksTable";

function Dot11MonitoringPage() {

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
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h3>All Monitored Networks</h3>

              <Dot11MonitoredNetworksTable />

              <a href={ApiRoutes.DOT11.MONITORING.CREATE} className="btn btn-secondary btn-sm">
                Create Monitored Network
              </a>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )
}

export default Dot11MonitoringPage;