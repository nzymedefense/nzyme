import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../App";
import {Presets} from "../../shared/timerange/TimeRange";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import ApiRoutes from "../../../util/ApiRoutes";
import NTPTransactionsTable from "../time/ntp/NTPTransactionsTable";

export default function AssetDetailsNTPTransactions({ title, filters }) {

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
                             internalLink={ApiRoutes.ETHERNET.TIME.NTP.INDEX + "?filters=" + JSON.stringify(filters)}
                             refreshAction={onRefresh} />

      <NTPTransactionsTable filters={filters} timeRange={timerange} revision={revision} perPage={10} />
    </>
  )

}