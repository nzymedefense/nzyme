import React, {useState} from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import SocksTunnelsTable from "./socks/SocksTunnelsTable";

export default function TunnelsPage() {

  const [socksTunnelsTimeRange, setSocksTunnelsTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Tunnels</h1>
          </div>
        </div>


        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="SOCKS Tunnels"
                                       timeRange={socksTunnelsTimeRange}
                                       setTimeRange={setSocksTunnelsTimeRange}/>

                <SocksTunnelsTable timeRange={socksTunnelsTimeRange} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
)

}