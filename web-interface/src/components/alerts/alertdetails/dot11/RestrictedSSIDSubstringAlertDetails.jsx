import React from "react";

import numeral from "numeral";

function RestrictedSSIDSubstringAlertDetails(props) {

  const alert = props.alert;

  return (
      <React.Fragment>
        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Details</h3>
                <dl>
                  <dt>SSID</dt>
                  <dd>{alert.attributes.ssid}</dd>
                  <dt>Restricted Substring</dt>
                  <dd>{alert.attributes.restricted_substring}</dd>

                  <p className="mb-0 mt-2">
                    A recorded SSID has a name that includes a restricted substring. Restricted substrings are
                    configured in the monitored network details.
                  </p>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Possible false-positives</h3>

                <p>
                  These circumstances can lead to false-positive alerts of this type:
                </p>

                <ul className="mb-0">
                  <li>A legitimate network could have a similar name. For example, if your network is called
                    &quot;ExampleNet&quot;, and another network of yours is called &quot;ExampleNet_Guest&quot;, a
                    restricted substring of &quot;Example&quot; would trigger the alert. Other monitored networks are
                    always considered legitimate and you should create monitoring configurations for all your networks
                    to avoid an alert in such a case.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default RestrictedSSIDSubstringAlertDetails;