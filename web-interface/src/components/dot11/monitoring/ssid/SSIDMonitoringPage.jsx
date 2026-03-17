import React, {useEffect, useState} from 'react';
import SectionMenuBar from "../../../shared/SectionMenuBar";
import {MONITORING_MENU_ITEMS} from "../Dot11MenuItems";
import ApiRoutes from "../../../../util/ApiRoutes";
import Dot11Service from "../../../../services/Dot11Service";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import {notify} from "react-notify-toast";
import SSIDMonitoringConfiguration from "./SSIDMonitoringConfiguration";
import KnownNetworksTable from "./KnownNetworksTable";
import usePageTitle from "../../../../util/UsePageTitle";

const dot11Service = new Dot11Service();

export default function SSIDMonitoringPage() {

  usePageTitle("SSID Monitoring");

  const [organizationId, tenantId] = useSelectedTenant();

  const [networks, setNetworks] = useState(null);

  const [revision, setRevision] = useState(new Date());

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setNetworks(null);
    dot11Service.findAllKnownNetworks(organizationId, tenantId, perPage, (page-1)*perPage, setNetworks)
  }, [page, organizationId, tenantId, revision])

  const onDeleteAll = (e) => {
    e.preventDefault();

    if (!confirm("Really delete all known networks? Each will reappear as unapproved network " +
      "next time nzyme records it.")) {
      return;
    }

    dot11Service.deleteAllKnownNetworks(organizationId, tenantId, () => {
      notify.show('All known networks deleted.', 'success');
      onChange();
    });
  }

  const onApproveAll = (e) => {
    e.preventDefault();

    if (!confirm("Really approve all known networks?")) {
      return;
    }

    dot11Service.approveAllKnownNetworks(organizationId, tenantId, () => {
      notify.show('All known networks approved.', 'success');
      onChange();
    });
  }

  const onChange = () => {
    setRevision(new Date());
  }

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

                <p className="text-muted">
                  The <i>minimum dwell time</i> sets how many minutes a SSID must be observed within the past 24 hours
                  before it is considered active. This prevents alerts for SSIDs of passing devices that were only
                  briefly visible. Set it to <i>0</i> to alert on any SSID, no matter how long it has been seen.
                </p>

                <p className="text-muted mb-0">
                  Newly detected networks should be treated as informational, not as critical alerts, until further
                  investigated and properly classified to ensure they don&apos;t pose a security risk.
                </p>
              </div>
            </div>
          </div>
        </div>

        <React.Fragment>
          <div className="row mt-3">
            <div className="col-xl-12 col-xxl-6">
              <div className="card">
                <div className="card-body">
                  <h3>Monitor Configuration</h3>

                  <SSIDMonitoringConfiguration organizationUUID={organizationId}
                                               tenantUUID={tenantId} />
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-xl-12 col-xxl-6">
              <div className="card">
                <div className="card-body">
                  <h3 style={{display: "inline-block"}}>Known SSIDs/Networks</h3>

                  <span className="float-end">
                  <button className="btn btn-secondary btn-sm me-1" onClick={onApproveAll}>Approve All</button>
                  <button className="btn btn-danger btn-sm" onClick={onDeleteAll}>Delete All</button>
                  </span>

                  <br style={{clear: "both"}}/>

                  <KnownNetworksTable networks={networks}
                                      onChange={onChange}
                                      page={page}
                                      setPage={setPage}
                                      perPage={perPage}/>
                </div>
              </div>
            </div>
          </div>
        </React.Fragment>
      </React.Fragment>
)

}