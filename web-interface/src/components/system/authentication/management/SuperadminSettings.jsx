import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ConfigurationValue from "../../../configuration/ConfigurationValue";
import ConfigurationModal from "../../../configuration/modal/ConfigurationModal";

const authenticationManagementService = new AuthenticationManagementService();

function SuperadminSettings() {

  const [configuration, setConfiguration] = useState(null);
  const [localRevision, setLocalRevision] = useState(0)

  useEffect(() => {
    authenticationManagementService.getGlobalSuperAdminConfiguration(setConfiguration);
  }, [localRevision]);

  if (!configuration) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped mb-0">
          <thead>
          <tr>
            <th>Configuration</th>
            <th>Value</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>Session Timeout (minutes)</td>
            <td>
              <ConfigurationValue value={configuration.session_timeout_minutes.value}
                                  configKey={configuration.session_timeout_minutes.key}
                                  required={configuration.session_timeout_minutes.requires_restart} />
            </td>
            <td>
              <ConfigurationModal config={configuration.session_timeout_minutes}
                                  setGlobalConfig={setConfiguration}
                                  setLocalRevision={setLocalRevision}
                                  dbUpdateCallback={authenticationManagementService.setGlobalSuperAdminConfiguration} />
            </td>
          </tr>
          <tr>
            <td>Session Inactivity Timeout (minutes)</td>
            <td>
              <ConfigurationValue value={configuration.session_inactivity_timeout_minutes.value}
                                  configKey={configuration.session_inactivity_timeout_minutes.key}
                                  required={configuration.session_inactivity_timeout_minutes.requires_restart} />
            </td>
            <td>
              <ConfigurationModal config={configuration.session_inactivity_timeout_minutes}
                                  setGlobalConfig={setConfiguration}
                                  setLocalRevision={setLocalRevision}
                                  dbUpdateCallback={authenticationManagementService.setGlobalSuperAdminConfiguration} />
            </td>
          </tr>
          <tr>
            <td>MFA Timeout (minutes)</td>
            <td>
              <ConfigurationValue value={configuration.mfa_timeout_minutes.value}
                                  configKey={configuration.mfa_timeout_minutes.key}
                                  required={configuration.mfa_timeout_minutes.requires_restart} />
            </td>
            <td>
              <ConfigurationModal config={configuration.mfa_timeout_minutes}
                                  setGlobalConfig={setConfiguration}
                                  setLocalRevision={setLocalRevision}
                                  dbUpdateCallback={authenticationManagementService.setGlobalSuperAdminConfiguration} />
            </td>
          </tr>
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default SuperadminSettings;