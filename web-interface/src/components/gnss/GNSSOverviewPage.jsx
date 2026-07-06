import React, {useEffect, useState} from 'react';
import GnssService from "../../services/GnssService";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import SectionMenuBar from "../shared/SectionMenuBar";
import {ASSETS_MENU_ITEMS} from "../ethernet/assets/AssetsMenuItems";
import ApiRoutes from "../../util/ApiRoutes";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import GNSSTapsTable from "./GNSSTapsTable";

const gnssService = new GnssService();

export default function GNSSOverviewPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [gnssTaps, setGnssTaps] = useState(null);
  const [showAll, setShowAll] = useState(false);

  useEffect(() => {
    setGnssTaps(null);
    gnssService.findGnssTaps(organizationId, tenantId, showAll, setGnssTaps)
  }, [organizationId, tenantId])

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-12">
          <h1>GNSS Overview</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Taps with GNSS Captures" />

              <GNSSTapsTable taps={gnssTaps} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}