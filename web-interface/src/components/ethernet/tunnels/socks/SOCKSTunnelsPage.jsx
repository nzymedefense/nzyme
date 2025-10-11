import React, {useContext, useEffect, useState} from "react";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import {Presets} from "../../../shared/timerange/TimeRange";
import SOCKSTunnelsTable from "./SOCKSTunnelsTable";
import {disableTapSelector, enableTapSelector} from "../../../misc/TapSelector";
import {TapContext} from "../../../../App";
import {SOCKS_FILTER_FIELDS} from "./SOCKSFilterFields";
import {queryParametersToFilters} from "../../../shared/filtering/FilterQueryParameters";
import {useLocation} from "react-router-dom";
import Filters from "../../../shared/filtering/Filters";
import {TUNNELS_MENU_ITEMS} from "../TunnelsMenuItems";
import ApiRoutes from "../../../../util/ApiRoutes";
import SectionMenuBar from "../../../shared/SectionMenuBar";

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

export default function SOCKSTunnelsPage() {

  const tapContext = useContext(TapContext);
  const urlQuery = useQuery();

  const [socksTunnelsTimeRange, setSocksTunnelsTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [socksTunnelsFilters, setSocksTunnelsFilters] = useState(
    queryParametersToFilters(urlQuery.get("filters"), SOCKS_FILTER_FIELDS)
  );

  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);


  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <SectionMenuBar items={TUNNELS_MENU_ITEMS}
                            activeRoute={ApiRoutes.ETHERNET.TUNNELS.SOCKS.INDEX} />
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>SOCKS Tunnels</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="All Tunnels"
                                       helpLink="https://go.nzyme.org/ethernet-socks"
                                       timeRange={socksTunnelsTimeRange}
                                       setTimeRange={setSocksTunnelsTimeRange}
                                       refreshAction={() => setRevision(new Date())} />

                <Filters filters={socksTunnelsFilters}
                         setFilters={setSocksTunnelsFilters}
                         fields={SOCKS_FILTER_FIELDS} />

                <hr />

                <SOCKSTunnelsTable timeRange={socksTunnelsTimeRange}
                                   filters={socksTunnelsFilters}
                                   setFilters={setSocksTunnelsFilters}
                                   revision={revision} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
)

}