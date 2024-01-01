import React from "react";
import {dot11FrequencyToChannel} from "../../../../util/Tools";
import numeral from "numeral";

function MonitorSignalTrackAlertDetails(props) {

  const alert = props.alert;

  return (
      <React.Fragment>
        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Details</h3>
                <dl>
                  <dt>Affected BSSID</dt>
                  <dd>{alert.attributes.bssid}</dd>
                  <dt>Affected Channel</dt>
                  <dd>
                    {dot11FrequencyToChannel(alert.attributes.channel)}{' '}
                    ({numeral(alert.attributes.channel).format("0,0")} MHz)
                  </dd>
                  <dt>Recorded by Tap</dt>
                  <dd>{alert.attributes.tap_name}</dd>

                  <p className="mb-0 mt-2">
                    One of the monitored access points is transmitting on multiple signal tracks. This might suggest
                    that an attacker is spoofing the station, resulting in signals with characteristics different from
                    the legitimate station. Typically, these differences arise from the distinct physical locations of
                    the attacker and the authentic station.
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
                  <li>A sudden shift in the radio frequency environment can lead to the emergence of new tracks. Observe
                    the signal track behavior over time to discern regular fluctuations in track activity.</li>
                  <li>A station with adaptive transmission power can lead to the detection of new tracks.</li>
                  <li>Relocating or modifying the configuration of the station can alter the signal characteristics and
                    result in the emergence of new tracks.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default MonitorSignalTrackAlertDetails;