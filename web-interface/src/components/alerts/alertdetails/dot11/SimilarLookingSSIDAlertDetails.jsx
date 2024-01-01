import React from "react";

import numeral from "numeral";

function SimilarLookingSSIDAlertDetails(props) {

  const alert = props.alert;

  return (
      <React.Fragment>
        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Details</h3>
                <dl>
                  <dt>Similar SSID</dt>
                  <dd>{alert.attributes.similar_ssid}</dd>
                  <dt>Similarity</dt>
                  <dd>{numeral(alert.attributes.similarity).format("0.[00]")}%</dd>
                  <dt>Alert Threshold</dt>
                  <dd>{alert.attributes.similarity_threshold}%</dd>

                  <p className="mb-0 mt-2">
                    A recorded SSID has a name that is too similar to the name of a monitored SSID. The similarity
                    threshold is configured in the monitored network configuration.
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
                  <li>The similarity threshold could be configured inappropriately. You may have to increase it.</li>
                  <li>A legitimate network could have a similar name. For example, if your network is called
                    &quot;Example&quot;, and another network of yours is called &quot;Example_Guest&quot;, some
                  threshold configurations may trigger an alarm. Other monitored networks are always considered
                  legitimate and you should create monitoring configurations for all your networks to avoid an alert
                  in such a case.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default SimilarLookingSSIDAlertDetails;