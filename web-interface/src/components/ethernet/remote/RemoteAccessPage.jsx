import React, {useContext, useEffect, useState} from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import SSHSessionsTable from "./ssh/SSHSessionsTable";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {TapContext} from "../../../App";

export default function RemoteAccessPage() {

  const tapContext = useContext(TapContext);

  const [sshSessionsTimeRange, setSshSessionsTimeRange] = useState(Presets.RELATIVE_HOURS_24);

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
            <h1>Remote Access Connections</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="SSH Sessions"
                                       helpLink="https://go.nzyme.org/ethernet-ssh"
                                       timeRange={sshSessionsTimeRange}
                                       setTimeRange={setSshSessionsTimeRange}/>

                <SSHSessionsTable timeRange={sshSessionsTimeRange} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}