import React from "react";
import DisconnectedClientsTable from "./DisconnectedClientsTable";
import ConnectedClientsTable from "./ConnectedClientsTable";

function ClientsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Clients</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Connected Clients <small>Last 15 minutes</small></h3>

                <p className="text-muted">
                  All clients currently observed as connected to an access point within range are listed here. The
                  advertised SSIDs of BSSIDs are compiled from the most recent three days of available data.
                </p>

                <ConnectedClientsTable />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Disconnected Clients <small>Last 15 minutes</small></h3>

                <p className="text-muted">
                  It should be noted that many modern WiFi devices utilize MAC address randomization when they are not
                  connected to a network. This practice improves privacy by complicating tracking efforts, resulting in
                  a wide range of distinct MAC addresses being captured by nzyme. The advertised SSIDs of BSSIDs are
                  compiled from the most recent three days of available data.
                </p>

                <DisconnectedClientsTable  />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ClientsPage;