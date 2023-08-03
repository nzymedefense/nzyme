import React, {useContext, useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../../util/ApiRoutes";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import Dot11Service from "../../../../services/Dot11Service";
import {TapContext} from "../../../../App";
import SignalStrength from "../../util/SignalStrength";
import InfrastructureTypes from "../../util/InfrastructureTypes";
import WPSInformation from "../../util/WPSInformation";
import SecuritySuites from "../../util/SecuritySuites";
import SSIDAdvertisementHistogram from "./SSIDAdvertisementHistogram";
import SSIDSignalWaterfallChart from "./SSIDSignalWaterfallChart";
import SSIDChannelUsageHistogram from "./SSIDChannelUsageHistogram";
import SSIDAccessPointClients from "./SSIDAccessPointClients";
import numeral from "numeral";
import {dot11FrequencyToChannel} from "../../../../util/Tools";
import ChannelSelector from "../../util/ChannelSelector";
import SSIDMonitoredInformation from "./SSIDMonitoredInformation";
import HelpBubble from "../../../misc/HelpBubble";

const dot11Service = new Dot11Service();
const DEFAULT_MINUTES = 15;

function SSIDDetailsPage() {

  const {bssidParam} = useParams();
  const {ssidParam} = useParams();
  const {frequencyParam} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [selectedFrequency, setSelectedFrequency] = useState(parseInt(frequencyParam, 10));

  const [ssid, setSSID] = useState(null);
  const [advertisementHistogramType, setHistogramAdvertisementType] = useState("beacon_count");

  useEffect(() => {
    dot11Service.findSSIDOfBSSID(bssidParam, ssidParam, DEFAULT_MINUTES, selectedTaps, setSSID);
  }, [])

  if (!ssid) {
    return <LoadingSpinner />
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-10">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
              <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.NETWORKS.BSSIDS}>Access Points</a></li>
              <li className="breadcrumb-item">{ssid.bssid}</li>
              <li className="breadcrumb-item">SSIDs</li>
              <li className="breadcrumb-item active" aria-current="page">{ssid.ssid}</li>
            </ol>
          </nav>
        </div>

        <div className="col-md-2">
          <a className="btn btn-primary float-end" href={ApiRoutes.DOT11.NETWORKS.BSSIDS}>Back</a>
        </div>

        <div className="col-md-12">
          <h1>
            Network Details
          </h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h3>
                SSID Information <small>Last 15 minutes</small>
              </h3>
              <dl className="mb-0">
                <dt>BSSID</dt>
                <dd>
                  {ssid.bssid} (Vendor: {ssid.bssid_oui ? ssid.bssid_oui : "Unknown"})
                </dd>
                <dt>Name</dt>
                <dd>{ssid.ssid}</dd>
                <dt>Signal Strength</dt>
                <dd>
                  <SignalStrength strength={ssid.signal_strength_average} selectedTapCount={selectedTaps.length} />
                </dd>
                <dt>Type</dt>
                <dd><InfrastructureTypes types={ssid.infrastructure_types} /></dd>
                <dt>Rates</dt>
                <dd>{ssid.rates ? ssid.rates.filter(n => n).join(", ") + " Mbps" : "N/A"}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="row">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <h3>
                    Security / Encryption <small>Last 15 minutes</small>
                  </h3>

                  <dl className="mb-0">
                    <dt>Protocol</dt>
                    <dd>{ssid.security_protocols.length === 0 ? "None" : ssid.security_protocols.join(",")}</dd>
                    <dt>WPS</dt>
                    <dd><WPSInformation wps={ssid.is_wps} /></dd>
                    <dt>Suite</dt>
                    <dd><SecuritySuites suites={ssid.security_suites} /></dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <h3>
                    Fingerprints <small>Last 15 minutes <HelpBubble link="https://go.nzyme.org/fingerprinting" /></small>
                  </h3>

                  <ul className="mb-0">
                  {ssid.fingerprints.map(function (fp) {
                    return <li key={"fp-" +fp}>{fp}</li>
                  })}
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <SSIDMonitoredInformation ssid={ssid} />

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3 style={{display: "inline-block"}}>
                SSID Advertisements <small>Last 24 hours maximum</small>
              </h3>

              <select className="form-select form-select-sm float-end" style={{width: 250}}
                      onChange={(e) => { setHistogramAdvertisementType(e.target.value) }}
                      value={advertisementHistogramType}>
                <option value="beacon_count">Beacon Count</option>
                <option value="proberesp_count">Probe Response Count</option>
              </select>

              <SSIDAdvertisementHistogram bssid={ssid.bssid} ssid={ssid.ssid} parameter={advertisementHistogramType} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="row">
            <div className="col-md-8">
              <div className="row">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>
                        Active Channels <small>Last 15 minutes</small>
                      </h3>

                      <SSIDChannelUsageHistogram bssid={ssid.bssid}
                                                 ssid={ssid.ssid}
                                                 minutes={15} />
                    </div>
                  </div>
                </div>
              </div>
              <div className="row mt-3">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3 style={{display: "inline-block"}}>
                        Signal Strength Waterfall for Channel {dot11FrequencyToChannel(selectedFrequency)}{' '}
                        ({numeral(selectedFrequency).format("0,0")} MHz) <small>Last 24 hours maximum</small>
                      </h3>

                      <div className="float-end">
                        <ChannelSelector currentFrequency={selectedFrequency}
                                         setFrequency={setSelectedFrequency}
                                         frequencies={ssid.frequencies} />
                      </div>

                      <SSIDSignalWaterfallChart bssid={ssid.bssid}
                                                ssid={ssid.ssid}
                                                frequency={selectedFrequency}
                                                minutes={24*60} />
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="row">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>
                        Clients connected to BSSID <small>Last 15 minutes</small>
                      </h3>

                      <p className="text-muted">
                        Please note that recording clients generally demands closer proximity, as
                        well as more precise matching of transmission-related parameters compared to recording access
                        points. Additionally, many modern devices will randomize their MAC addresses and hide vendor
                        information.
                      </p>

                      <SSIDAccessPointClients clients={ssid.access_point_clients} />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}

export default SSIDDetailsPage;