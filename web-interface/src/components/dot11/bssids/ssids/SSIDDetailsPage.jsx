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

const dot11Service = new Dot11Service();
const DEFAULT_MINUTES = 15;

function SSIDDetailsPage() {

  const {bssidParam} = useParams();
  const {ssidParam} = useParams();
  const {frequencyParam} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

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
              <h3>SSID Information</h3>
              <dl className="mb-0">
                <dt>BSSID</dt>
                <dd>
                  {ssid.bssid} (Vendor: {ssid.bssid_oui ? ssid.bssid_oui : "Unknown"})
                </dd>
                <dt>Name</dt>
                <dd>{ssid.ssid}</dd>
                <dt>Signal Strength</dt>
                <dd><SignalStrength strength={ssid.signal_strength_average} /></dd>
                <dt>Type</dt>
                <dd><InfrastructureTypes types={ssid.infrastructure_types} /></dd>
                <dt>Rates</dt>
                <dd>{ssid.rates ? ssid.rates.filter(n => n).join(", ") + " Mbps" : "N/A"}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h3>Security / Encryption</h3>
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

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3 style={{display: "inline-block"}}>SSID Advertisements</h3>


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
          <div className="card">
            <div className="card-body">
              <h3>Active Channels</h3>

              todo
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3>Signal Strength Waterfall</h3>

              <SSIDSignalWaterfallChart bssid={ssid.bssid}
                                        ssid={ssid.ssid}
                                        frequency={frequencyParam}
                                        minutes={24*60} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}

export default SSIDDetailsPage;