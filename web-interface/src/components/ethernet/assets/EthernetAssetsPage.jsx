import React, {useState} from "react";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import {ASSETS_MENU_ITEMS} from "./AssetsMenuItems";

export default function EthernetAssetsPage() {

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

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
                <CardTitleWithControls title="Asset List"
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange}/>

              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )


}