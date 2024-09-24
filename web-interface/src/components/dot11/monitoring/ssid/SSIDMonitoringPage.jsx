import React from 'react';
import SectionMenuBar from "../../../shared/SectionMenuBar";
import {MONITORING_MENU_ITEMS} from "../Dot11MenuItems";
import ApiRoutes from "../../../../util/ApiRoutes";
import SSIDMonitoringProxy from "./SSIDMonitoringProxy";

export default function SSIDMonitoringPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <SectionMenuBar items={MONITORING_MENU_ITEMS}
                            activeRoute={ApiRoutes.DOT11.MONITORING.SSIDS.INDEX}/>
          </div>

          <div className="col-md-2">
            <a href="https://go.nzyme.org/wifi-ssid-monitoring" className="btn btn-secondary float-end">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>SSID / Network Name Monitoring</h3>

                <p className="text-muted">
                  Monitoring SSIDs (wireless network names) in range provides situational awareness, allowing you to
                  manually verify that no potentially malicious networks are nearby—especially those that
                  similar-sounding or restricted SSID monitoring might miss. It also enables you to detect new,
                  unexpected networks, such as a printer automatically starting its own network for device
                  adoption, or unauthorized mobile hotspots which could introduce vulnerabilities without your
                  knowledge.
                </p>

                <p className="text-muted mb-0">
                  Newly detected networks should be treated as informational, not as critical alerts, until further
                  investigated and properly classified to ensure they don&apos;t pose a security risk.
                </p>
              </div>
            </div>
          </div>
        </div>

        <SSIDMonitoringProxy />
      </React.Fragment>
)

}