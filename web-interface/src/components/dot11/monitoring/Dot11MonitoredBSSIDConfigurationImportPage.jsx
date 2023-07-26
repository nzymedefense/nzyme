import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import Dot11Service from "../../../services/Dot11Service";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ApiRoutes from "../../../util/ApiRoutes";

const dot11Service = new Dot11Service();
function Dot11MonitoredBSSIDConfigurationImportPage() {

  const {uuid} = useParams();

  const [ssid, setSSID] = useState(null);

  useEffect(() => {
    dot11Service.findMonitoredSSID(uuid, setSSID, function() {
      // noop
    });
  }, [uuid]);

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
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item">Monitored Networks</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(ssid.uuid)}>{ssid.ssid}</a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Configuration Import</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(ssid.uuid)}>Back</a>
          </div>

          <div className="col-md-12">
            <h1>
              Import Monitoring Configuration for Network &quot;{ssid.ssid}&quot;
            </h1>
          </div>

          <div className="row mt-3">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <p className="text-muted">
                    Setting up network monitoring manually for large networks with multiple access points can be
                    labor-intensive. To streamline this process, nzyme's import functionality allows you to select known
                    networks and import their recorded configurations. However, it's crucial that you cross-check the
                    imported configuration with a known-good state to prevent the accidental import of BSSIDs and other
                    data associated with potential bad actors already active within the network environment.
                  </p>

                  <div className="alert alert-info mb-0">This feature is not implemented yet.</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default Dot11MonitoredBSSIDConfigurationImportPage;