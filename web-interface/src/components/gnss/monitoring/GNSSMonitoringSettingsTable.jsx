import React, {useEffect, useState} from "react";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import GnssService from "../../../services/GnssService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ConfigurationValue from "../../configuration/ConfigurationValue";
import ConfigurationModal from "../../configuration/modal/ConfigurationModal";

const gnssService = new GnssService();

export default function GNSSMonitoringSettingsTable() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [configuration, setConfiguration] = useState(null);
  const [localRevision, setLocalRevision] = useState(new Date());

  useEffect(() => {
    gnssService.getMonitoringConfiguration(organizationId, tenantId, setConfiguration)
  }, [organizationId, tenantId, localRevision])

  if (!configuration) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
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
            <td>Training Period (minutes)</td>
            <td>
              <ConfigurationValue value={configuration.gnss_monitoring_training_period_minutes.value}
                                  configKey={configuration.gnss_monitoring_training_period_minutes.key} />
            </td>
            <td>
              <ConfigurationModal config={configuration.gnss_monitoring_training_period_minutes}
                                  setGlobalConfig={setConfiguration}
                                  setLocalRevision={setLocalRevision}
                                  organizationId={organizationId}
                                  tenantId={tenantId}
                                  dbUpdateCallback={gnssService.updateMonitoringConfiguration} />
            </td>
          </tr>
          </tbody>
        </table>
      </React.Fragment>
  )

}