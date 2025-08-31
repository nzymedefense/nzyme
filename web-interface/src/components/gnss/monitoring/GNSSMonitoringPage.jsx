import React, {useEffect, useState} from "react";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import ApiRoutes from "../../../util/ApiRoutes";
import GnssService from "../../../services/GnssService";
import GNSSMonitoringRulesTable from "./GNSSMonitoringRulesTable";

const gnssService = new GnssService();

export default function GNSSMonitoringPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [rules, setRules] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 25;

  useEffect(() => {
    gnssService.findAllMonitoringRules(organizationId, tenantId, perPage, (page-1)*perPage, setRules)
  }, [organizationId, tenantId, page]);

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <h1>GNSS Monitoring</h1>
          </div>

          <div className="col-md-2 text-end">
            <a href="https://go.nzyme.org/gnss-monitoring" className="btn btn-secondary">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Monitoring Rules" />

                <GNSSMonitoringRulesTable page={page} setPage={setPage} rules={rules} />

                <div className="mt-2">
                  <a href={ApiRoutes.GNSS.MONITORING.RULES.CREATE(organizationId, tenantId)}
                     className="btn btn-sm btn-secondary">
                    Create GNSS Monitoring Rule
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}