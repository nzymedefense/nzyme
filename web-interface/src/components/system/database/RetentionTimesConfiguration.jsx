import React, {useEffect, useState} from "react";
import ConfigurationValue from "../../configuration/ConfigurationValue";
import LoadingSpinner from "../../misc/LoadingSpinner";
import SystemService from "../../../services/SystemService";
import ConfigurationModal from "../../configuration/modal/ConfigurationModal";

const systemService = new SystemService();

function RetentionTimesConfiguration() {

  const [configuration, setConfiguration] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);

  useEffect(() => {
    setConfiguration(null);
    systemService.getDatabaseSummary(setConfiguration)
  }, [localRevision])

  if (!configuration) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <p>
          Data retention cleaning operations occur once every hour. Any modifications made to the retention periods
          will take effect following the next scheduled data retention cleaning run.
        </p>

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
            <td>802.11 / WiFi Data Retention</td>
            <td>
              <ConfigurationValue value={configuration.dot11_retention_time_days.value}
                                  configKey={configuration.dot11_retention_time_days.key}
                                  required={true} /> days
            </td>
            <td>
              <ConfigurationModal config={configuration.dot11_retention_time_days}
                                  setGlobalConfig={setConfiguration}
                                  setLocalRevision={setLocalRevision}
                                  dbUpdateCallback={systemService.updateRetentionTimes} />
            </td>
          </tr>
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default RetentionTimesConfiguration;