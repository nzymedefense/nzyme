import React, {useEffect, useState} from 'react';
import LoadingSpinner from "../../../misc/LoadingSpinner";
import Dot11Service from "../../../../services/Dot11Service";
import ConfigurationValue from "../../../configuration/ConfigurationValue";
import ConfigurationModal from "../../../configuration/modal/ConfigurationModal";

const dot11Service = new Dot11Service();

export default function SSIDMonitoringConfiguration(props) {

  const organizationUUID = props.organizationUUID;
  const tenantUUID = props.tenantUUID;

  const [configuration, setConfiguration] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);

  useEffect(() => {
    setConfiguration(null);
    dot11Service.getSSIDMonitoringConfiguration(
        organizationUUID, tenantUUID, setConfiguration
    );
  }, [localRevision, organizationUUID, tenantUUID]);

  if (!configuration) {
    return <LoadingSpinner />
  }

  return (
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
                                setLocalRevision={setLocalRevision}
                                organizationId={organizationUUID}
                                tenantId={tenantUUID}
                                dbUpdateCallback={dot11Service.updateSSIDMonitoringConfiguration}/>
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
                                setLocalRevision={setLocalRevision}
                                organizationId={organizationUUID}
                                tenantId={tenantUUID}
                                dbUpdateCallback={dot11Service.updateSSIDMonitoringConfiguration}/>
          </td>
        </tr>

        <tr>
          <td>Minimum continuous network dwell time</td>
          <td>5 minutes</td>
          <td className="text-muted">n/a</td>
        </tr>
        </tbody>
      </table>
  )

}