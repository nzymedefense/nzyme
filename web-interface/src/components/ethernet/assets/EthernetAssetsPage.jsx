import React, {useState} from "react";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";

export default function EthernetAssetsPage() {

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <h1>Tunnels</h1>
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