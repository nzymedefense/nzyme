import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ConfigurationValue from "../../../configuration/ConfigurationValue";
import ConfigurationModal from "../../../configuration/modal/ConfigurationModal";
import CryptoService from "../../../../services/CryptoService";

const cryptoService = new CryptoService();

function PGPConfiguration(props) {

  const crypto = props.crypto;

  const [configuration, setConfiguration] = useState(null)

  useEffect(function () {
    if (crypto) {
      setConfiguration(crypto.pgp_configuration);
    } else {
      setConfiguration(null);
    }
  }, [crypto])

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
          <td>{configuration.pgp_sync_enabled.key_human_readable}</td>
          <td>
            <ConfigurationValue value={configuration.pgp_sync_enabled.value}
                                configKey={configuration.pgp_sync_enabled.key}
                                required={true}
                                boolean={true} />
          </td>
          <td>
            <ConfigurationModal config={configuration.pgp_sync_enabled}
                                setGlobalConfig={setConfiguration}
                                setLocalRevision={props.setLocalRevision}
                                dbUpdateCallback={cryptoService.updatePGPConfiguration} />
          </td>
        </tr>
        </tbody>
      </table>
  )

}

export default PGPConfiguration;