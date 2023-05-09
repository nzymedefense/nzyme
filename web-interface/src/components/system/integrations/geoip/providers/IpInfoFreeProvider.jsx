import React from "react";
import ConfigurationValue from "../../../../configuration/ConfigurationValue";
import ConfigurationModal from "../../../../configuration/modal/ConfigurationModal";
import EncryptedConfigurationValue from "../../../../configuration/EncryptedConfigurationValue";

function IpInfoFreeProvider(props) {

  const summary = props.summary;

  return (
      <React.Fragment>
        <hr className="mt-3" />

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
              <EncryptedConfigurationValue isSet={true}
                                           configKey="foo"
                                           required={true} />
            </td>
            <td>
              <a href="#">Edit</a>
            </td>
          </tr>
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default IpInfoFreeProvider;