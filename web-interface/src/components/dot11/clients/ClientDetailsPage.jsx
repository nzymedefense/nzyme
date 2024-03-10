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
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import Dot11MacAddress from "../../shared/context/macs/Dot11MacAddress";
import MacAddressContextLine from "../../shared/context/macs/details/MacAddressContextLine";
import TapBasedSignalStrengthTable from "../shared/TapBasedSignalStrengthTable";
import ClientSignalStrengthChart from "./ClientSignalStrengthChart";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";

const dot11Service = new Dot11Service();

function ClientDetailsPage() {

  const {macParam} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [client, setClient] = useState(null);

  const [discoPairsTimeRange, setDiscoPairsTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  useEffect(() => {
    setClient(null);
    dot11Service.findMergedConnectedOrDisconnectedClient(macParam, selectedTaps, setClient);
  }, [selectedTaps]);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

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
                <li className="breadcrumb-item active" aria-current="page">{client.mac.address}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={ApiRoutes.DOT11.CLIENTS.INDEX}>Back</a>
          </div>

          <div className="col-md-12">
            <h1>
              Client &quot;{client.mac.address} ({client.mac.oui ? client.mac.oui : "Unknown Vendor"})&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Client Information"
                                       fixedAppliedTimeRange={Presets.ALL_TIME} />

                <dl className="mb-0">
                  <dt>MAC Address</dt>
                  <dd>
                    <Dot11MacAddress addressWithContext={client.mac} showOui={true}/>
                  </dd>
                  <dt>Last Connected to BSSID</dt>
                  <dd>
                    {client.connected_bssid ? <Dot11MacAddress addressWithContext={client.connected_bssid.mac}
                                                               href={ApiRoutes.DOT11.NETWORKS.BSSID(client.connected_bssid.mac.address)}
                                                               showOui={true}/> : "None"}{' '}
                  </dd>
                  <dt>Context</dt>
                  <dd>
                    <MacAddressContextLine address={client.mac.address} context={client.mac.context}/>
                  </dd>
                </dl>
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Activity"
                                       fixedAppliedTimeRange={Presets.ALL_TIME} />

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
                <CardTitleWithControls title="Observed Connections to BSSIDs"
                                       fixedAppliedTimeRange={Presets.ALL_TIME} />

                <ClientBSSIDHistory connectedBSSID={client.connected_bssid ? client.connected_bssid : null}
                                    bssids={client.connected_bssid_history}/>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Observed Probe Requests"
                                       fixedAppliedTimeRange={Presets.ALL_TIME} />

                <ObservedProbeRequestsList probeRequests={client.probe_requests}/>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Average Data &amp; Control Frames Signal Strengths By Tap"
                                           fixedAppliedTimeRange={Presets.RELATIVE_MINUTES_15} />

                    <TapBasedSignalStrengthTable strengths={client.connected_signal_strength}/>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Data &amp; Control Frames Signal Strength"
                                           fixedAppliedTimeRange={Presets.RELATIVE_HOURS_24} />

                    <ClientSignalStrengthChart data={client.connected_signal_strength_histogram} />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Average Probe Request Frames Signal Strengths By Tap"
                                           fixedAppliedTimeRange={Presets.RELATIVE_MINUTES_15} />

                    <TapBasedSignalStrengthTable strengths={client.disconnected_signal_strength}/>
                  </div>
                </div>
              </div>
            </div>
            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Probe Request Frames Signal Strength"
                                           fixedAppliedTimeRange={Presets.RELATIVE_HOURS_24} />

                    <ClientSignalStrengthChart data={client.disconnected_signal_strength_histogram} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Data &amp; Control Frames"
                                       fixedAppliedTimeRange={Presets.RELATIVE_HOURS_24} />

                <ClientActivityHistogram histogram={client.activity_histogram}
                                         parameter="connected_frames"
                                         type="line"/>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Probe Request Frames"
                                       fixedAppliedTimeRange={Presets.RELATIVE_HOURS_24} />

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
                <CardTitleWithControls title="Disconnection Frames"
                                       fixedAppliedTimeRange={Presets.RELATIVE_HOURS_24} />

                <ClientActivityHistogram histogram={client.activity_histogram}
                                         parameter="disconnection_activity"
                                         type="bar"/>

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
                <CardTitleWithControls title="Top Disconnection Pairs"
                                       timeRange={discoPairsTimeRange}
                                       setTimeRange={setDiscoPairsTimeRange} />

                <DiscoPairsTable bssids={[client.mac.address]} highlightValue={client.mac} timeRange={discoPairsTimeRange} />

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