import React, {useEffect, useState} from 'react';
import SectionMenuBar from "../../../shared/SectionMenuBar";
import {MONITORING_MENU_ITEMS} from "../Dot11MenuItems";
import ApiRoutes from "../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import Dot11CustomMonitoringRulesTable from "./Dot11CustomMonitoringRulesTable";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";

export default function Dot11CustomMonitoringPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [rules, setRules] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 25;

  useEffect(() => {
    setRules({total: 0, rules: []})
  }, [organizationId, tenantId, page, perPage])

  return (
    <>
      <div className="row">
        <div className="col-md-10">
          <SectionMenuBar items={MONITORING_MENU_ITEMS}
                          activeRoute={ApiRoutes.DOT11.MONITORING.CUSTOM.INDEX} />
        </div>

        <div className="col-md-2">
          <a href="https://go.nzyme.org/wifi-custom-monitoring" className="btn btn-secondary float-end">Help</a>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-xl-12 col-xxl-6">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Custom WiFi Monitoring Rules" />

              <Dot11CustomMonitoringRulesTable page={page} setPage={setPage} perPage={perPage} rules={rules} />

              <div className="mt-2">
                <a href={ApiRoutes.DOT11.MONITORING.CUSTOM.CREATE} className="btn btn-sm btn-secondary">
                  Create WiFi Monitoring Rule
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )

}