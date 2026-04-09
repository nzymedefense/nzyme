import React, {useEffect, useState} from 'react';
import usePageTitle from "../../../util/UsePageTitle";
import SectionMenuBar from "../../shared/SectionMenuBar";
import {MONITORING_MENU_ITEMS} from "./Dot11MenuItems";
import ApiRoutes from "../../../util/ApiRoutes";
import MonitorsTable from "../../monitors/shared/MonitorsTable";
import MonitorsService from "../../../services/MonitorsService";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";

const monitorsService = new MonitorsService();

export default function Dot11MonitorsPage() {

  usePageTitle("WiFi Monitors");

  const [organizationId, tenantId] = useSelectedTenant();

  const [monitors, setMonitors] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 25;

  useEffect(() => {
    monitorsService.findAllOfType(
      "DOT11_BSSID", organizationId, tenantId, perPage, (page-1)*perPage, setMonitors
    )
  }, [page, perPage, organizationId, tenantId])

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-10">
          <SectionMenuBar items={MONITORING_MENU_ITEMS}
                          activeRoute={ApiRoutes.DOT11.MONITORING.MONITORS.INDEX}/>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <h3 style={{display: "inline-block"}}>Configured Monitors</h3>

              <MonitorsTable monitors={monitors} page={page} setPage={setPage} perPage={perPage} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}