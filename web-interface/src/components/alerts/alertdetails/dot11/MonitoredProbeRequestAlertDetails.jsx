import React from 'react';
import ApiRoutes from "../../../../util/ApiRoutes";
import Dot11MacAddress from "../../../shared/context/macs/Dot11MacAddress";
import AutomaticDot11MacAddressLink from "../../../shared/context/macs/AutomaticDot11MacAddressLink";
import {CLIENT} from "../../../shared/context/macs/Dot11MacAddressType";

export default function MonitoredProbeRequestAlertDetails(props) {

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

                  <dt>Client MAC Address</dt>
                  <dd>
                    <AutomaticDot11MacAddressLink type={CLIENT} mac={alert.attributes.client_mac} />
                  </dd>

                  <p className="mb-0 mt-3">
                    A monitored SSID in a probe request was detected.
                  </p>

                  <p className="mb-0 mt-3">
                    Monitoring probe requests is essential for ensuring that sensitive SSIDs are not broadcasted by
                    your devices, especially in secure or sensitive locations. For instance, if you provide WiFi
                    access
                    in a high-security area, it is crucial to prevent users from inadvertently revealing that they
                    have previously connected to this network elsewhere. By using nzyme to monitor probe requests,
                    you can enforce a policy that requires users to delete certain networks from their devices or
                    disable the &quot;auto-connect&quot; feature.
                  </p>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
          <div className="card">
              <div className="card-body">
                <h3>Possible false-positives</h3>

                <p className="text-muted">
                  These circumstances can lead to false-positive alerts of this type:
                </p>

                <ul className="mb-0">
                  <li className="text-muted">n/a</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}