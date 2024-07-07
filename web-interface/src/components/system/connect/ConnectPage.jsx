import React, {useEffect, useState} from "react";
import ConfigurationValue from "../../configuration/ConfigurationValue";
import ConfigurationModal from "../../configuration/modal/ConfigurationModal";
import EncryptedConfigurationValue from "../../configuration/EncryptedConfigurationValue";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ConnectService from "../../../services/ConnectService";
import ConnectStatus from "./ConnectStatus";

const connectService = new ConnectService();

export default function ConnectPage() {

  const [configuration, setConfiguration] = useState(null)
  const [localRevision, setLocalRevision] = useState(0)

  const [status, setStatus] = useState(null);

  useEffect(() => {
    connectService.getConfiguration(setConfiguration)
    connectService.getStatus(setStatus);
  }, [localRevision])

  if (!configuration || !status) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>nzyme Connect</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-3">
            <ConnectStatus status={status}/>
          </div>
          <div className="col-md-9">
            <div className="card">
              <div className="card-body">
                <p className="mb-3">
                  The nzyme Connect APIs provide up-to-date, curated operational data for your nzyme setups, including:
                </p>

                <ul>
                  <li>GeoIP information</li>
                  <li>MAC address vendor (<i>OUI</i>) information</li>
                  <li>Bluetooth vendor and device information</li>
                </ul>

                <p className="mb-0">
                  You can get your free API key
                  at <a href="https://connect.nzyme.org/" target="_blank">https://connect.nzyme.org/</a>.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
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
                    <td>nzyme Connect enabled</td>
                    <td>
                      <ConfigurationValue value={configuration.connect_enabled.value}
                                          configKey={configuration.connect_enabled.key}
                                          required={true}
                                          boolean={true}/>
                    </td>
                    <td>
                      <ConfigurationModal config={configuration.connect_enabled}
                                          setGlobalConfig={setConfiguration}
                                          setLocalRevision={setLocalRevision}
                                          dbUpdateCallback={connectService.updateConfiguration}/>
                    </td>
                  </tr>
                  <tr>
                    <td>API Key</td>
                    <td>
                      <EncryptedConfigurationValue isSet={configuration.connect_api_key.value_is_set}
                                                   configKey={configuration.connect_api_key.key}
                                                   required={true}/>
                    </td>
                    <td>
                      <ConfigurationModal config={configuration.connect_api_key}
                                          setGlobalConfig={setConfiguration}
                                          setLocalRevision={setLocalRevision}
                                          dbUpdateCallback={connectService.updateConfiguration}/>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}