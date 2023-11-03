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
import DiscoPairsTable from "../disco/DiscoPairsTable";

const dot11Service = new Dot11Service();

function ClientDetailsPage() {

  const {macParam} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [client, setClient] = useState(null);

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
                    <span className="dot11-mac">{client.mac}</span> ({client.mac_oui ? client.mac_oui : "Unknown Vendor"})
                  </dd>
                  <dt>Connected to BSSID</dt>
                  <dd>
                    {client.connected_bssid ? <a href={ApiRoutes.DOT11.NETWORKS.BSSID(client.connected_bssid.bssid)} className="dot11-mac">{client.connected_bssid.bssid}</a> : "None"}{' '}
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
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>
                  Observed Connections to BSSIDs <small>All Time</small>
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

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Data &amp; Control Frames <small>Last 24 hours</small></h3>

                <ClientActivityHistogram histogram={client.activity_histogram}
                                         parameter="connected_frames"
                                         type="line"/>

                <p className="text-muted mb-0 mt-3">
                  <i>Connected</i> means that the client was connected to an access point. The frame types include data and
                  control frames.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Probe Request Frames <small>Last 24 hours</small></h3>

                <ClientActivityHistogram histogram={client.activity_histogram}
                                         parameter="disconnected_frames"
                                         type="bar"/>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Disconnection Frames <small>Last 24 hours</small></h3>

                <ClientActivityHistogram histogram={client.activity_histogram}
                                         parameter="disconnection_activity"
                                         type="bar" />

                <p className="text-muted mb-0 mt-3">
                  Disconnection activity refers to the sum of deauthentication and disassociation frames.

                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3 className="mb-0">Top Disconnection Pairs <small>Last 24 hours</small></h3>

                <DiscoPairsTable bssids={[client.mac]} highlightValue={client.mac} />

                <p className="mb-0 mt-3 text-muted">
                  The MAC address of this client is <span className="highlighted">highlighted.</span>
                </p>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ClientDetailsPage;