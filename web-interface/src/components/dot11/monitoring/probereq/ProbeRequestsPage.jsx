import React from 'react';
import SectionMenuBar from "../../../shared/SectionMenuBar";
import ApiRoutes from "../../../../util/ApiRoutes";
import ProbeRequestsTableProxy from "./ProbeRequestsTableProxy";
import {MONITORING_MENU_ITEMS} from "../Dot11MenuItems";

export default function ProbeRequestsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <SectionMenuBar items={MONITORING_MENU_ITEMS}
                            activeRoute={ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.INDEX}/>
          </div>

          <div className="col-md-2">
            <a href="https://go.nzyme.org/wifi-probereq-monitoring" className="btn btn-secondary float-end">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Probe Request Monitoring</h3>

                <p className="text-muted mb-0">
                  Monitoring probe requests is essential for ensuring that sensitive SSIDs are not broadcasted by
                  your devices, especially in secure or sensitive locations. For instance, if you provide WiFi
                  access in a high-security area, it is crucial to prevent users from inadvertently revealing
                  that they have previously connected to this network elsewhere. By using nzyme to monitor probe
                  requests, you can enforce a policy that requires users to delete certain networks from their
                  devices or disable the &quot;auto-connect&quot; feature.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Monitored Probe Requests</h3>

                <ProbeRequestsTableProxy />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
)

}