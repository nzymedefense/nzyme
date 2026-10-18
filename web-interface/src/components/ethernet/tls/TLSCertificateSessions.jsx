import React, {useState} from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets} from "../../shared/timerange/TimeRange";
import ApiRoutes from "../../../util/ApiRoutes";
import TLSSessionsTable from "./TLSSessionsTable";

export default function TLSCertificateSessions({title, filters}) {

  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [revision, setRevision] = useState(new Date());

  const onRefresh = () => {
    setRevision(new Date());
  }

  return (
      <React.Fragment>
        <CardTitleWithControls title={title}
                               doNotPersistTimeRange={true}
                               timeRange={timeRange}
                               setTimeRange={setTimeRange}
                               internalLink={ApiRoutes.ETHERNET.TLS.SESSIONS.INDEX + "?filters=" + JSON.stringify(filters)}
                               refreshAction={onRefresh} />

        <TLSSessionsTable timeRange={timeRange}
                          filters={filters}
                          setFilters={() => {}}
                          revision={revision}
                          perPage={10} />
      </React.Fragment>
  )

}
