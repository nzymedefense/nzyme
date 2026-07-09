import React, {useEffect, useState} from 'react';
import GnssService from "../../services/GnssService";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import SectionMenuBar from "../shared/SectionMenuBar";
import {ASSETS_MENU_ITEMS} from "../ethernet/assets/AssetsMenuItems";
import ApiRoutes from "../../util/ApiRoutes";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import GNSSTapsTable from "./GNSSTapsTable";
import moment from "moment";

const gnssService = new GnssService();

export default function GNSSOverviewPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [gnssTaps, setGnssTaps] = useState(null);
  const [showAll, setShowAll] = useState(false);

  useEffect(() => {
    setGnssTaps(null);
    gnssService.findGnssTaps(organizationId, tenantId, showAll, setGnssTaps)
  }, [organizationId, tenantId, showAll])

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-10">
          <h1>GNSS Overview</h1>
        </div>
        <div className="col-2">
          <a href="https://go.nzyme.org/gnss-overview" className="btn btn-secondary float-end" target="_blank">Help</a>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title={!showAll ? "Taps with active GNSS Captures" : "Taps with and without active GNSS Captures"} />

              <div className="form-check form-switch mb-1">
                <input className="form-check-input"
                       type="checkbox"
                       role="switch"
                       id="showAll"
                       onChange={(e) => setShowAll(e.target.checked)}
                       checked={showAll} />
                <label className="form-check-label" htmlFor="showAll">
                  Show All Taps
                </label>
              </div>

              <GNSSTapsTable taps={gnssTaps} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}