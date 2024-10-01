import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import Dot11Service from "../../../../services/Dot11Service";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../util/ApiRoutes";
import ConfigurationValue from "../../../configuration/ConfigurationValue";
import ConfigurationModal from "../../../configuration/modal/ConfigurationModal";

const dot11Service = new Dot11Service();

export default function MonitoredClientsConfigurationPage() {

  const { uuid } = useParams();

  const [ssid, setSSID] = useState(null);
  const [configuration, setConfiguration] = useState(null);
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    dot11Service.findMonitoredSSID(uuid, setSSID, () => {  });
  }, [uuid]);

  useEffect(() => {
    if (ssid) {
      dot11Service.getMonitoredClientsConfiguration(uuid, setConfiguration);
    }
  }, [ssid, revision]);

  // We are using a custom update callback because we have to pass the SSID UUID.
  const onConfigurationUpdate = (newConfig, successCallback, errorCallback) => {
    dot11Service.updateMonitoredClientsConfiguration(newConfig, uuid, successCallback, errorCallback);
  }

  if (!ssid || !configuration) {
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
                <li className="breadcrumb-item active" aria-current="page">Allowed Clients</li>
              </ol>
            </nav>
          </div>
          <div className="col-md-5">
            <a className="btn btn-primary float-end" href={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(uuid)}>Back</a>
          </div>
        </div>

        <div className="col-md-12">
          <h1>Allowed Clients</h1>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Configuration</h3>

                <table className="table table-sm table-hover table-striped">
                  <thead>
                  <tr>
                    <th>Configuration</th>
                    <th>Value</th>
                    <th>Actions</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr>
                    <td>Is monitoring enabled</td>
                    <td>
                      <ConfigurationValue value={configuration.is_enabled.value}
                                          configKey={configuration.is_enabled.key}
                                          boolean={true}/>
                    </td>
                    <td>
                      <ConfigurationModal config={configuration.is_enabled}
                                          setGlobalConfig={setConfiguration}
                                          setLocalRevision={setRevision}
                                          dbUpdateCallback={onConfigurationUpdate}/>
                    </td>
                  </tr>

                  <tr>
                    <td>Is event generation enabled</td>
                    <td>
                      <ConfigurationValue value={configuration.eventing_is_enabled.value}
                                          configKey={configuration.eventing_is_enabled.key}
                                          boolean={true}/>
                    </td>
                    <td>
                      <ConfigurationModal config={configuration.eventing_is_enabled}
                                          setGlobalConfig={setConfiguration}
                                          setLocalRevision={setRevision}
                                          dbUpdateCallback={onConfigurationUpdate}/>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Clients</h3>

                <p>
                  All clients that have ever been detected as connected and communicating with an access point (BSSID)
                  of this monitored network.
                </p>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}