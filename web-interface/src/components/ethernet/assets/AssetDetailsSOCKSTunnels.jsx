import React, {useContext, useEffect, useState} from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import {TapContext} from "../../../App";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import ApiRoutes from "../../../util/ApiRoutes";
import SOCKSTunnelsTable from "../tunnels/socks/SOCKSTunnelsTable";

export default function AssetDetailsSOCKSTunnels({title, filters}) {

  const tapContext = useContext(TapContext);

  const [timerange, setTimerange] = useState(Presets.RELATIVE_HOURS_24);
  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  const onRefresh = () => {
    setRevision(new Date());
  }

  return (
      <>
        <CardTitleWithControls title={title}
                               timeRange={timerange}
                               setTimeRange={setTimerange}
                               internalLink={ApiRoutes.ETHERNET.TUNNELS.SOCKS.INDEX + "?filters=" + JSON.stringify(filters)}
                               refreshAction={onRefresh} />

        <SOCKSTunnelsTable timeRange={timerange}
                           filters={filters}
                           revision={revision}
                           perPage={10} />
      </>
  )

}