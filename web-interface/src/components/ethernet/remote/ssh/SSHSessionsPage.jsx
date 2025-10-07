import React, {useContext, useEffect, useState} from "react";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import {Presets} from "../../../shared/timerange/TimeRange";
import SSHSessionsTable from "./SSHSessionsTable";
import {disableTapSelector, enableTapSelector} from "../../../misc/TapSelector";
import {TapContext} from "../../../../App";
import {SSH_FILTER_FIELDS} from "./SSHFilterFields";
import Filters from "../../../shared/filtering/Filters";
import {useLocation} from "react-router-dom";
import {queryParametersToFilters} from "../../../shared/filtering/FilterQueryParameters";
import {REMOTE_ACCESS_MENU_ITEMS} from "../RemoteAccessMenuItems";
import ApiRoutes from "../../../../util/ApiRoutes";
import SectionMenuBar from "../../../shared/SectionMenuBar";

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

export default function SSHSessionsPage() {

  const tapContext = useContext(TapContext);
  const urlQuery = useQuery();

  const [sshSessionsTimeRange, setSshSessionsTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [sshSessionsFilters, setSshSessionsFilters] = useState(
    queryParametersToFilters(urlQuery.get("filters"), SSH_FILTER_FIELDS)
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
            <SectionMenuBar items={REMOTE_ACCESS_MENU_ITEMS}
                            activeRoute={ApiRoutes.ETHERNET.REMOTE.SSH.INDEX} />
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>SSH Sessions</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="All Sessions"
                                       helpLink="https://go.nzyme.org/ethernet-ssh"
                                       timeRange={sshSessionsTimeRange}
                                       setTimeRange={setSshSessionsTimeRange}
                                       refreshAction={() => setRevision(new Date())} />

                <Filters filters={sshSessionsFilters}
                         setFilters={setSshSessionsFilters}
                         fields={SSH_FILTER_FIELDS} />

                <hr />

                <SSHSessionsTable timeRange={sshSessionsTimeRange}
                                  filters={sshSessionsFilters}
                                  setFilters={setSshSessionsFilters}
                                  revision={revision} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}