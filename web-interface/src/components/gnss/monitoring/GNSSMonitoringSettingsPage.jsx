import React from "react";
import SectionMenuBar from "../../shared/SectionMenuBar";
import {GNSS_MONITORING_MENU_ITEMS} from "./GNSSMonitoringMenuItems";
import ApiRoutes from "../../../util/ApiRoutes";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import GNSSMonitoringSettingsTable from "./GNSSMonitoringSettingsTable";

export default function GNSSMonitoringSettingsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <SectionMenuBar items={GNSS_MONITORING_MENU_ITEMS}
                            activeRoute={ApiRoutes.GNSS.MONITORING.SETTINGS} />
          </div>

          <div className="col-md-2 text-end">
            <a href="https://go.nzyme.org/gnss-monitoring" className="btn btn-secondary">Help</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Monitoring Settings" />

                <GNSSMonitoringSettingsTable />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}