import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import Dot11MonitoredNetworksTable from "./Dot11MonitoredNetworksTable";
import Dot11BanditsTable from "./Dot11BanditsTable";

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

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3>Bandits</h3>

              <p className="text-muted">
                All nzyme taps are constantly looking for known attack platforms, called <i>bandits</i>. The table below
                gives you an overview of which types of bandits are detected. Once a bandit is detected, an alarm is
                raised.
              </p>

              <Dot11BanditsTable />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )
}

export default Dot11MonitoringPage;