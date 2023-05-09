import React, {useEffect, useState} from "react";
import EncryptedConfigurationValue from "../../../../configuration/EncryptedConfigurationValue";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import IntegrationsService from "../../../../../services/IntegrationsService";
import ConfigurationModal from "../../../../configuration/modal/ConfigurationModal";

const integrationsService = new IntegrationsService();

function IpInfoFreeProvider(props) {

  const activeProvider = props.activeProvider;
  const activateProvider = props.activateProvider;

  const PROVIDER_ID = "ipinfo_free";

  const [configuration, setConfiguration] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);

  const configurationComplete = function() {
    return configuration && configuration.token.value_is_set;
  }

  useEffect(() => {
    integrationsService.getGeoIpIpInfoFreeConfiguration(setConfiguration);
  }, [localRevision])

  if (!configuration) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="geoip-provider-configuration mt-3">
          <h3>Provider: IPinfo.io Free</h3>

          <p>
            All IPinfo.io providers regularly download the GeoIP lookup data from the IPinfo.io servers to avoid
            latency-heavy individual API calls over the internet. Everyone can sign up for the free plan and all you
            need is the API token you will be provided after you registered.
          </p>

          <h4>Provided Data</h4>

          <ul>
            <li>Address types: IPv4, IPv6</li>
            <li>Country name and code</li>
            <li>AS number, name and domain</li>
          </ul>
        </div>

        <h4>Configuration</h4>

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
            <td>API Token</td>
            <td>
              <EncryptedConfigurationValue isSet={configuration.token.value_is_set}
                                           configKey={configuration.token.key}
                                           required={true} />
            </td>
            <td>
              <ConfigurationModal config={configuration.token}
                                  setGlobalConfig={setConfiguration}
                                  setLocalRevision={setLocalRevision}
                                  dbUpdateCallback={integrationsService.updateGeoIpIpInfoFreeConfiguration} />
            </td>
          </tr>
          </tbody>
        </table>

        { activeProvider === PROVIDER_ID ?
            null : <button className="btn btn-sm btn-primary"
                onClick={() => activateProvider(PROVIDER_ID)}
                disabled={!configurationComplete()}>
              Activate Provider
        </button> }
      </React.Fragment>
  )

}

export default IpInfoFreeProvider;