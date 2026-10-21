import React, {useContext} from "react";
import numeral from "numeral";
import {toast} from "react-toastify";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import usePageTitle from "../../../util/UsePageTitle";
import {BEACONS_MENU_ITEMS} from "./BeaconsMenuItems";
import BeaconsConfiguration from "./BeaconsConfiguration";
import BeaconsService from "../../../services/ethernet/BeaconsService";
import {UserContext} from "../../../App";
import {userHasPermission} from "../../../util/Tools";

const beaconsService = new BeaconsService();

export default function BeaconsSettingsPage() {

  usePageTitle("Ethernet Beacons Settings");

  const [organizationId, tenantId] = useSelectedTenant();
  const user = useContext(UserContext);
  const canManage = userHasPermission(user, "ethernet_monitoring_manage");

  const onDeleteAll = (e) => {
    e.preventDefault();

    if (!confirm("Really delete ALL beacons of this tenant, including their check-in history and notes? " +
        "This cannot be undone. Beacons are detected again under new names if the devices keep calling out.")) {
      return;
    }

    beaconsService.deleteAllBeacons(organizationId, tenantId, (response) => {
      toast.success(numeral(response.data.deleted_count).format("0,0") + " beacons deleted.");
    });
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-10">
          <SectionMenuBar items={BEACONS_MENU_ITEMS}
                          activeRoute={ApiRoutes.ETHERNET.BEACONS.SETTINGS} />
        </div>

        <div className="col-md-2">
          <a href="https://go.nzyme.org/ethernet-beacons-config" className="btn btn-secondary float-end">Help</a>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Detection" />

              <p className="text-muted">
                Detection runs every minute and looks at all connections in the detection window. A device is
                beaconing when it connected to the same destination at least the minimum number of times, with a
                median interval above the minimum interval, and when at least the minimum regularity share of its
                intervals stayed within the maximum jitter around the median. Larger windows find slower beacons but
                need the raw data to be retained that long.
              </p>

              <BeaconsConfiguration organizationId={organizationId} tenantId={tenantId} section="detection" />
            </div>
          </div>
        </div>

        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Data Sources" />

              <p className="text-muted">
                Which traffic is analyzed for beacons. TLS beacons are grouped by server name, SOCKS beacons by
                tunneled destination and DNS beacons by registered domain. Everything else is grouped by destination
                address and port. Protocol traffic that is periodic by design, like NTP and STUN, is always ignored.
              </p>

              <BeaconsConfiguration organizationId={organizationId} tenantId={tenantId} section="sources" />
            </div>
          </div>

          <div className="card mt-3">
            <div className="card-body">
              <CardTitleWithControls title="Alerting" />

              <p className="text-muted">
                Detection alerts raised by beacon detection. All beacon alerts are disabled by default. Alerts on
                suspicious and malicious activity require the beacon to be classified accordingly first.
              </p>

              <BeaconsConfiguration organizationId={organizationId} tenantId={tenantId} section="alerting" />
            </div>
          </div>

          {canManage ?
            <div className="card mt-3">
              <div className="card-body">
                <CardTitleWithControls title="Delete All Beacons" slim={true} />

                <p>
                  Deletes every beacon of this tenant, including its check-in history and notes. Names and
                  classifications are lost. Device and asset data is not affected. Exclusions and the settings on
                  this page are kept. Beacons are detected again under new names if the devices keep calling out,
                  unless an exclusion matches them.
                </p>

                <button className="btn btn-sm btn-danger" onClick={onDeleteAll}>Delete All Beacons</button>
              </div>
            </div>
            : null}
        </div>
      </div>
    </React.Fragment>
  )

}
