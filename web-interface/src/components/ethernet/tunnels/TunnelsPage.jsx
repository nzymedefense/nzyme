import React, {useContext, useEffect, useState} from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import SocksTunnelsTable from "./socks/SocksTunnelsTable";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {TapContext} from "../../../App";

export default function TunnelsPage() {

  const tapContext = useContext(TapContext);

  const [socksTunnelsTimeRange, setSocksTunnelsTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);


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
                <CardTitleWithControls title="SOCKS Tunnels"
                                       helpLink="https://go.nzyme.org/ethernet-socks"
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