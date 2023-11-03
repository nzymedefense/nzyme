import React from "react";

function MonitorDiscoAnomalyAlertDetails(props) {

  const alert = props.alert;

  return (
      <React.Fragment>
        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Details</h3>
                <dl>
                  <dt>Recorded by Tap</dt>
                  <dd>{alert.attributes.tap_name}</dd>

                  <p className="mb-0 mt-2">
                    One of the monitored access points experienced an unusually high number of
                    deauthentication/disassociation events. Such events are commonly associated with malicious WiFi
                    attacks, indicating a potential ongoing threat.
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
                  <li>The deauthentication anomaly detection method for the monitored network may not be selected or
                  configured optimally, resulting in mis-identification of anomalies.</li>
                  <li>A sudden increase in WiFi clients can lead to an increase in legitimate deauthentication traffic
                  that could be mis-identified as an anomaly.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default MonitorDiscoAnomalyAlertDetails;