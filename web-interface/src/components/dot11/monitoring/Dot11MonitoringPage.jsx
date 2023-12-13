import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import MonitoredNetworksTable from "./MonitoredNetworksTable";
import BuiltinBanditsTable from "./bandits/BuiltinBanditsTable";
import CustomBanditsTableProxy from "./bandits/CustomBanditsTableProxy";

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

              <MonitoredNetworksTable />

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
                All nzyme taps are constantly looking for known attack platforms, called <i>bandits</i>. Once a bandit
                is detected, an alarm is raised.
              </p>

              <h4>Built-In Bandits</h4>

              <p className="text-muted">
                The built-in bandits ship with nzyme by default.
              </p>

              <BuiltinBanditsTable />

              <h4 className="mt-4">Custom Bandits</h4>

              <p className="text-muted">
                You can define and manage your own bandit definitions.
              </p>

              <CustomBanditsTableProxy />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )
}

export default Dot11MonitoringPage;