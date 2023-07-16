import React, {useContext, useEffect, useState} from "react";
import ConnectedClientsTable from "./ConnectedClientsTable";
import {TapContext} from "../../../App";
import Dot11Service from "../../../services/Dot11Service";
import DisconnectedClientsTable from "./DisconnectedClientsTable";
import ClientHistogram from "./ClientHistogram";

const dot11Service = new Dot11Service();
const MINUTES = 15;

function ClientsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [connectedClients, setConnectedClients] = useState(null);
  const [disconnectedClients, setDisconnectedClients] = useState(null);
  const [histograms, setHistograms] = useState(null);

  const perPage = 10;
  const [connectedClientsPage, setConnectedClientsPage] = useState(1);
  const [disconnectedClientsPage, setDisconnectedClientsPage] = useState(1);

  useEffect(() => {
    setConnectedClients(null);
    setDisconnectedClients(null);

    dot11Service.getClientHistograms(selectedTaps, setHistograms);
    dot11Service.findAllClients(MINUTES, selectedTaps, setConnectedClients, setDisconnectedClients, perPage,
        (connectedClientsPage-1)*perPage, perPage,(disconnectedClientsPage-1)*perPage);
  }, [selectedTaps, connectedClientsPage, disconnectedClientsPage])

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Clients</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Connected Clients</h3>

                <ClientHistogram param="connected" histograms={histograms} />
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Disconnected Clients</h3>

                <ClientHistogram param="disconnected" histograms={histograms} />
              </div>
            </div>
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

                <ConnectedClientsTable clients={connectedClients} minutes={MINUTES} perPage={perPage} page={connectedClientsPage} setPage={setConnectedClientsPage} />
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

                <DisconnectedClientsTable clients={disconnectedClients} minutes={MINUTES} perPage={perPage} page={disconnectedClientsPage} setPage={setDisconnectedClientsPage} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ClientsPage;