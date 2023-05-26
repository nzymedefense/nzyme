import React, {useEffect, useState} from "react";
import IntegrationsService from "../../../../services/IntegrationsService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ConfigurationValue from "../../../configuration/ConfigurationValue";
import ConfigurationModal from "../../../configuration/modal/ConfigurationModal";
import EncryptedConfigurationValue from "../../../configuration/EncryptedConfigurationValue";

const integrationsService = new IntegrationsService();

function SmtpIntegration() {

  const [configuration, setConfiguration] = useState(null);

  const [localRevision, setLocalRevision] = useState(0);

  useEffect(() => {
    setConfiguration(null);
    integrationsService.getSmtpConfiguration(setConfiguration);
  }, [])

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
          <td>Transport Strategy</td>
          <td>
            <ConfigurationValue value={configuration.smtp_transport_strategy.value}
                                configKey={configuration.smtp_transport_strategy.key}
                                required={true} />
          </td>
          <td>
            <ConfigurationModal config={configuration.smtp_transport_strategy}
                                setGlobalConfig={setConfiguration}
                                setLocalRevision={setLocalRevision}
                                dbUpdateCallback={null} />
          </td>
        </tr>
        </tbody>
      </table>
    </React.Fragment>
  )

}

export default SmtpIntegration;