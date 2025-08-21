import React, {useEffect, useState} from "react";
import ConfigurationValue from "../configuration/ConfigurationValue";
import ConfigurationModal from "../configuration/modal/ConfigurationModal";
import SystemService from "../../services/SystemService";
import LoadingSpinner from "../misc/LoadingSpinner";
import AuthenticationManagementService from "../../services/AuthenticationManagementService";

const systemService = new SystemService();
const authMgmtService = new AuthenticationManagementService();

export default function SubsystemsConfiguration(props) {

  const dbUpdateCallback = props.dbUpdateCallback;

  // Optional.
  const organizationUUID = props.organizationUUID;
  const tenantUUID = props.tenantUUID;

  const [configuration, setConfiguration] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);

  useEffect(() => {
    setConfiguration(null);

    if (!organizationUUID && !tenantUUID) {
      systemService.getSubsystemsConfiguration(setConfiguration);
      return;
    }

    if (organizationUUID && !tenantUUID) {
      authMgmtService.getSubsystemsConfigurationOfOrganization(organizationUUID, setConfiguration);
      return
    }

    if (organizationUUID && tenantUUID) {
      authMgmtService.getSubsystemsConfigurationOfTenantOfOrganization(organizationUUID, tenantUUID, setConfiguration);
      return;
    }
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
        <td>Is Ethernet enabled</td>
        <td>
          <ConfigurationValue value={configuration.subsystem_ethernet_enabled.value}
                              configKey={configuration.subsystem_ethernet_enabled.key}
                              boolean={true}/>
        </td>
        <td>
          <ConfigurationModal config={configuration.subsystem_ethernet_enabled}
                              disabled={!configuration.subsystem_ethernet_available}
                              disabledTitle={"Subsystem is disabled system-wide."}
                              setGlobalConfig={setConfiguration}
                              setLocalRevision={setLocalRevision}
                              organizationId={organizationUUID}
                              tenantId={tenantUUID}
                              dbUpdateCallback={dbUpdateCallback} />
        </td>
      </tr>
      <tr>
        <td>Is WiFi/802.11 enabled</td>
        <td>
          <ConfigurationValue value={configuration.subsystem_dot11_enabled.value}
                              configKey={configuration.subsystem_dot11_enabled.key}
                              boolean={true}/>
        </td>
        <td>
          <ConfigurationModal config={configuration.subsystem_dot11_enabled}
                              disabled={!configuration.subsystem_dot11_available}
                              disabledTitle={"Subsystem is disabled system-wide."}
                              setGlobalConfig={setConfiguration}
                              setLocalRevision={setLocalRevision}
                              organizationId={organizationUUID}
                              tenantId={tenantUUID}
                              dbUpdateCallback={dbUpdateCallback}/>
        </td>
      </tr>
      <tr>
        <td>Is Bluetooth enabled</td>
        <td>
          <ConfigurationValue value={configuration.subsystem_bluetooth_enabled.value}
                              configKey={configuration.subsystem_bluetooth_enabled.key}
                              boolean={true}/>
        </td>
        <td>
          <ConfigurationModal config={configuration.subsystem_bluetooth_enabled}
                              disabled={!configuration.subsystem_bluetooth_available}
                              disabledTitle={"Subsystem is disabled system-wide."}
                              setGlobalConfig={setConfiguration}
                              setLocalRevision={setLocalRevision}
                              organizationId={organizationUUID}
                              tenantId={tenantUUID}
                              dbUpdateCallback={dbUpdateCallback}/>
        </td>
      </tr>
      <tr>
        <td>Is UAV (Unmanned Aerial Vehicle) enabled</td>
        <td>
          <ConfigurationValue value={configuration.subsystem_uav_enabled.value}
                              configKey={configuration.subsystem_uav_enabled.key}
                              boolean={true}/>
        </td>
        <td>
          <ConfigurationModal config={configuration.subsystem_uav_enabled}
                              disabled={!configuration.subsystem_uav_available}
                              disabledTitle={"Subsystem is disabled system-wide."}
                              setGlobalConfig={setConfiguration}
                              setLocalRevision={setLocalRevision}
                              organizationId={organizationUUID}
                              tenantId={tenantUUID}
                              dbUpdateCallback={dbUpdateCallback}/>
        </td>
      </tr>
      <tr>
        <td>Is GNSS (Global Navigation Satellite System) enabled</td>
        <td>
          <ConfigurationValue value={configuration.subsystem_gnss_enabled.value}
                              configKey={configuration.subsystem_gnss_enabled.key}
                              boolean={true}/>
        </td>
        <td>
          <ConfigurationModal config={configuration.subsystem_gnss_enabled}
                              disabled={!configuration.subsystem_gnss_available}
                              disabledTitle={"Subsystem is disabled system-wide."}
                              setGlobalConfig={setConfiguration}
                              setLocalRevision={setLocalRevision}
                              organizationId={organizationUUID}
                              tenantId={tenantUUID}
                              dbUpdateCallback={dbUpdateCallback}/>
        </td>
      </tr>
      </tbody>
    </table>
  )

}