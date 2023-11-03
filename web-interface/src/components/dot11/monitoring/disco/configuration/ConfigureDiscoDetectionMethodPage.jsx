import React, {useEffect, useState} from "react";
import ApiRoutes from "../../../../../util/ApiRoutes";
import {Navigate, useParams} from "react-router-dom";
import DetectionMethodDialogProxy from "./DetectionMethodDialogProxy";
import Dot11Service from "../../../../../services/Dot11Service";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import {notify} from "react-notify-toast";

const dot11Service = new Dot11Service();

function ConfigureDiscoDetectionMethodPage() {

  const { uuid } = useParams();

  const [ssid, setSSID] = useState(null);

  const [configuration, setConfiguration] = useState(null);
  const [selectedMethod, setSelectedMethod] = useState(null);

  const [redirect, setRedirect] = useState(false);

  const onSubmit = (e, method_type, configuration) => {
    dot11Service.setDiscoDetectionConfiguration(method_type, configuration, uuid,
        () => {
          notify.show("Deauthentication anomaly detection method updated.", "success");
          setRedirect(true);
        })
  }

  useEffect(() => {
    dot11Service.findMonitoredSSID(uuid, setSSID, () => {});
    dot11Service.getDiscoDetectionConfiguration(uuid, setConfiguration)
  }, [uuid]);

  useEffect(() => {
    if (configuration) {
      setSelectedMethod(configuration.method_type);
    }
  }, [configuration]);

  if (redirect) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(uuid)} />
  }

  if (!ssid || !configuration || !selectedMethod) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-7">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item">Monitored Networks</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(ssid.uuid)}>{ssid.ssid}</a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Deauthentication Monitor</li>
              </ol>
            </nav>
          </div>
          <div className="col-md-5">
            <a className="btn btn-primary float-end" href={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(uuid)}>Back</a>
          </div>
        </div>

        <div className="col-md-12">
          <h1>
            Deauthentication Detection Method
          </h1>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <div className="card">
              <div className="card-body">
                <h3>Configure Anomaly Detection Method</h3>

                <select className="form-select"
                        value={selectedMethod}
                        name="method"
                        onChange={(e) => setSelectedMethod(e.target.value)}>
                  <option value="NOOP">Disabled Anomaly Detection</option>
                  <option value="STATIC_THRESHOLD">Static Threshold</option>
                </select>

                <div className="mt-3">
                  <DetectionMethodDialogProxy type={selectedMethod}
                                              configuration={configuration}
                                              monitoredNetworkId={uuid}
                                              onSubmit={onSubmit} />
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <p className="text-muted">
                  Anomaly detection operates for each tap individually and doesn't use aggregated data. Any triggered
                  alerts will include the specific tap information.
                </p>

                <p className="text-muted mb-0">
                  When configuring the anomaly detection method, always assume that data for each tap is processed
                  separately. Use the tap selector on
                  the <a href={ApiRoutes.DOT11.DISCO.INDEX}>deauthentication page</a> to inspect recorded deauthentication
                  traffic of individual taps.
                </p>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ConfigureDiscoDetectionMethodPage;