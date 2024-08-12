import React, {useContext, useEffect, useState} from "react";
import ConnectedClientsTable from "./ConnectedClientsTable";
import {TapContext} from "../../../App";
import Dot11Service from "../../../services/Dot11Service";
import DisconnectedClientsTable from "./DisconnectedClientsTable";
import ClientHistogram from "./ClientHistogram";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {Presets} from "../../shared/timerange/TimeRange";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import moment from "moment";

const dot11Service = new Dot11Service();
const MINUTES = 15;

function ClientsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [connectedClientsHistogramTimeRange, setConnectedClientsHistogramTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [connectedClientsHistogram, setConnectedClientsHistogram] = useState(null);
  const [disconnectedClientsHistogramTimeRange, setDisconnectedClientsHistogramTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [disconnectedClientsHistogram, setDisconnectedClientsHistogram] = useState(null);

  const [connectedClients, setConnectedClients] = useState(null);
  const [connectedClientsTimeRange, setConnectedClientsTimeRange] = useState(Presets.RELATIVE_MINUTES_15);
  const [connectedClientsPage, setConnectedClientsPage] = useState(1);

  const [disconnectedClients, setDisconnectedClients] = useState(null);
  const [disconnectedClientsTimeRange, setDisconnectedClientsTimeRange] = useState(Presets.RELATIVE_MINUTES_15);
  const [disconnectedClientsPage, setDisconnectedClientsPage] = useState(1);

  const [disconnectedClientsSkipRandomized, setDisconnectedClientsSkipRandomized] = useState(true);

  const perPage = 25;

  useEffect(() => {
    setConnectedClientsHistogram(null);

    dot11Service.getConnectedClientsHistogram(
        connectedClientsHistogramTimeRange, selectedTaps, setConnectedClientsHistogram
    );
  }, [connectedClientsHistogramTimeRange, selectedTaps])

  useEffect(() => {
    setDisconnectedClientsHistogram(null);

    dot11Service.getDisconnectedClientsHistogram(
        disconnectedClientsHistogramTimeRange, disconnectedClientsSkipRandomized, selectedTaps, setDisconnectedClientsHistogram
    );
  }, [disconnectedClientsHistogramTimeRange, disconnectedClientsSkipRandomized, selectedTaps])

  useEffect(() => {
    setConnectedClients(null);

    dot11Service.findConnectedClients(connectedClientsTimeRange, selectedTaps, setConnectedClients,
        perPage, (connectedClientsPage-1)*perPage);
  }, [connectedClientsPage, connectedClientsTimeRange, selectedTaps])

  useEffect(() => {
    setDisconnectedClients(null);

    dot11Service.findDisconnectedClients(
        disconnectedClientsTimeRange, disconnectedClientsSkipRandomized, selectedTaps, setDisconnectedClients,
        perPage, (disconnectedClientsPage-1)*perPage);
  }, [disconnectedClientsPage, disconnectedClientsTimeRange, disconnectedClientsSkipRandomized, selectedTaps])

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  const disconnectedTitle = () => {
    if (disconnectedClientsSkipRandomized) {
      return "Disconnected Clients (Excluding Randomized)"
    } else {
      return "Disconnected Clients"
    }
  }

  const onDisconnectedClientsSkipRandomizedChange = () => {
    setDisconnectedClientsSkipRandomized((!disconnectedClientsSkipRandomized));
  }

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
                <CardTitleWithControls title="Connected Clients" slim={true}
                                       timeRange={connectedClientsHistogramTimeRange}
                                       setTimeRange={setConnectedClientsHistogramTimeRange} />

                <ClientHistogram histogram={connectedClientsHistogram} />
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title={disconnectedTitle()} slim={true}
                                       timeRange={disconnectedClientsHistogramTimeRange}
                                       setTimeRange={setDisconnectedClientsHistogramTimeRange} />

                <ClientHistogram histogram={disconnectedClientsHistogram} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Connected Clients"
                                       timeRange={connectedClientsTimeRange}
                                       setTimeRange={setConnectedClientsTimeRange} />

                <p className="text-muted">
                  All clients currently observed as connected to an access point within range are listed here. The
                  advertised SSIDs of BSSIDs are compiled from the most recent three days of available data.
                </p>

                <ConnectedClientsTable clients={connectedClients}
                                       minutes={MINUTES}
                                       perPage={perPage}
                                       page={connectedClientsPage}
                                       setPage={setConnectedClientsPage} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title={disconnectedTitle()}
                                       timeRange={disconnectedClientsTimeRange}
                                       setTimeRange={setDisconnectedClientsTimeRange}/>

                <p className="text-muted">
                  It should be noted that many modern WiFi devices utilize MAC address randomization when they are not
                  connected to a network. This practice improves privacy by complicating tracking efforts, resulting in
                  a wide range of distinct MAC addresses being captured by nzyme. The advertised SSIDs of BSSIDs are
                  compiled from the most recent three days of available data.
                </p>

                <div className="form-check form-switch mb-3">
                  <input className="form-check-input"
                         type="checkbox"
                         role="switch"
                         id="skipRandomized"
                         onChange={onDisconnectedClientsSkipRandomizedChange}
                         checked={disconnectedClientsSkipRandomized}/>
                  <label className="form-check-label"
                         htmlFor="skipRandomized">
                    Exclude Clients with Randomized MAC Address
                  </label>
                </div>

                <DisconnectedClientsTable clients={disconnectedClients}
                                          minutes={MINUTES}
                                          perPage={perPage}
                                          page={disconnectedClientsPage}
                                          setPage={setDisconnectedClientsPage}/>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ClientsPage;