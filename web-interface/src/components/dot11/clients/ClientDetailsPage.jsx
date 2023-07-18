import React, {useContext, useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../util/ApiRoutes";
import Dot11Service from "../../../services/Dot11Service";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {TapContext} from "../../../App";
import moment from "moment";
import ClientBSSIDHistory from "../util/ClientBSSIDHistory";
import ObservedProbeRequestsList from "./ObservedProbeRequestsList";
import ClientActivityHistogram from "./ClientActivityHistogram";

const dot11Service = new Dot11Service();

function ClientDetailsPage() {

  const {macParam} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [client, setClient] = useState(null);
  const [activityHistogramType, setActivityHistogramType] = useState("total_frames");

  useEffect(() => {
    dot11Service.findMergedConnectedOrDisconnectedClient(macParam, selectedTaps, setClient);
  }, []);

  if (!client) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.CLIENTS.INDEX}>Clients</a></li>
                <li className="breadcrumb-item active" aria-current="page">{client.mac}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={ApiRoutes.DOT11.CLIENTS.INDEX}>Back</a>
          </div>

          <div className="col-md-12">
            <h1>
              Client &quot;{client.mac} ({client.mac_oui ? client.mac_oui : "Unknown Vendor"})&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>
                  Client Information <small>All Time</small>
                </h3>
                <dl className="mb-0">
                  <dt>MAC Address</dt>
                  <dd>
                    {client.mac} ({client.mac_oui ? client.mac_oui : "Unknown Vendor"})
                  </dd>
                  <dt>Connected to BSSID</dt>
                  <dd>
                    {client.connected_bssid ? client.connected_bssid.bssid : "None"}{' '}
                    {client.connected_bssid ?
                      (client.connected_bssid.oui ? "(" + client.connected_bssid.oui + ")" : "(Unknown Vendor)") : null}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>
                  Activity <small>All Time</small>
                </h3>
                <dl className="mb-0">
                  <dt>First Seen</dt>
                  <dd>
                    {moment(client.first_seen).format()}{' '}
                    (Note: 802.11/WiFi data retention time is {client.data_retention_days} days)
                  </dd>
                  <dt>Last Seen</dt>
                  <dd>{moment(client.last_seen).format()}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3 style={{display: "inline-block"}}>
                  Recorded Frames <small>Last 24 hours</small>
                </h3>

                <select className="form-select form-select-sm float-end" style={{width: 150}}
                        onChange={(e) => { setActivityHistogramType(e.target.value) }}
                        value={activityHistogramType}>
                  <option value="total_frames">All Frames</option>
                  <option value="connected_frames">Data Frames</option>
                  <option value="disconnected_frames">Probe Requests</option>
                </select>

                <ClientActivityHistogram histogram={client.activity_histogram} parameter={activityHistogramType} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>
                  Observed Connections BSSIDs <small>All Time</small>
                </h3>

                <ClientBSSIDHistory connectedBSSID={client.connected_bssid ? client.connected_bssid.bssid : null}
                                    bssids={client.connected_bssid_history} />
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>
                  Observed Probe Requests <small>All Time</small>
                </h3>

                <ObservedProbeRequestsList probeRequests={client.probe_requests} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ClientDetailsPage;