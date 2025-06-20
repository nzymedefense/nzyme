import React, {useEffect, useState} from "react";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import {ASSETS_MENU_ITEMS} from "./AssetsMenuItems";
import AssetsService from "../../../services/ethernet/AssetsService";
import AssetsTable from "./AssetsTable";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";

const assetsService = new AssetsService();

export default function EthernetAssetsPage() {

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [assets, setAssets] = useState(null);
  const [page, setPage] = useState(1);
  const perPage = 25;

  const [orderColumn, setOrderColumn] = useState("last_seen");
  const [orderDirection, setOrderDirection] = useState("DESC");

  const [organizationId, tenantId] = useSelectedTenant();

  useEffect(() => {
    setAssets(null);
    if (organizationId && tenantId) {
      assetsService.findAllAssets(organizationId, tenantId, timeRange, orderColumn, orderDirection, perPage, (page-1)*perPage, setAssets);
    }
  }, [organizationId, tenantId, timeRange, orderColumn, orderDirection, page]);

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <SectionMenuBar items={ASSETS_MENU_ITEMS}
                            activeRoute={ApiRoutes.ETHERNET.ASSETS.INDEX} />
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Asset Filters"
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange}/>

              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Assets" />

                <AssetsTable assets={assets}
                             page={page}
                             setPage={setPage}
                             perPage={perPage}
                             orderColumn={orderColumn}
                             setOrderColumn={setOrderColumn}
                             orderDirection={orderDirection}
                             setOrderDirection={setOrderDirection} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )


}