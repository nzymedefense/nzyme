import React, {useEffect, useState} from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import {ASSETS_MENU_ITEMS} from "./AssetsMenuItems";
import AssetsService from "../../../services/ethernet/AssetsService";
import AssetsTable from "./AssetsTable";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import {useLocation} from "react-router-dom";
import {queryParametersToFilters} from "../../shared/filtering/FilterQueryParameters";
import {ASSET_FILTER_FIELDS} from "./AssetFilterFields";
import Filters from "../../shared/filtering/Filters";
import ActiveAssetsHistogram from "./ActiveAssetsHistogram";
import LatestAssetsHistogram from "./LatestAssetsHistogram";
import DisappearedAssetsHistogram from "./DisappearedAssetsHistogram";

const assetsService = new AssetsService();

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

export default function EthernetAssetsPage() {

  const [organizationId, tenantId] = useSelectedTenant();

  const urlQuery = useQuery();

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [assets, setAssets] = useState(null);
  const [page, setPage] = useState(1);
  const perPage = 25;

  const [orderColumn, setOrderColumn] = useState("last_seen");
  const [orderDirection, setOrderDirection] = useState("DESC");

  const [revision, setRevision] = useState(new Date());

  const [filters, setFilters] = useState(
    queryParametersToFilters(urlQuery.get("filters"), ASSET_FILTER_FIELDS)
  );

  useEffect(() => {
    setAssets(null);
    if (organizationId && tenantId) {
      assetsService.findAllAssets(organizationId, tenantId, timeRange, orderColumn, orderDirection, filters, perPage, (page-1)*perPage, setAssets);
    }
  }, [organizationId, tenantId, timeRange, orderColumn, orderDirection, filters, page, revision]);

  return (
      <React.Fragment>
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
                                       setTimeRange={setTimeRange} />

                <Filters filters={filters}
                         setFilters={setFilters}
                         fields={ASSET_FILTER_FIELDS} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Active Assets"
                                       fixedAppliedTimeRange={timeRange}
                                       refreshAction={() => setRevision(new Date())} />

                <ActiveAssetsHistogram organizationId={organizationId}
                                       tenantId={tenantId}
                                       filters={filters}
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange}
                                       revision={revision} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Newest Assets"
                                       fixedAppliedTimeRange={timeRange}
                                       refreshAction={() => setRevision(new Date())} />

                <LatestAssetsHistogram organizationId={organizationId}
                                       tenantId={tenantId}
                                       filters={filters}
                                       timeRange={timeRange}
                                       revision={revision} />
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Recently Disappeared Assets"
                                       fixedAppliedTimeRange={timeRange}
                                       refreshAction={() => setRevision(new Date())} />

                <DisappearedAssetsHistogram organizationId={organizationId}
                                            tenantId={tenantId}
                                            filters={filters}
                                            timeRange={timeRange}
                                            revision={revision} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Assets"
                                       fixedAppliedTimeRange={timeRange}
                                       refreshAction={() => setRevision(new Date())} />

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