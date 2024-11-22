import React, {useEffect, useState} from "react";
import SystemService from "../../../services/SystemService";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import GlobalDatabaseUsageTable from "./GlobalDatabaseUsageTable";
import {notify} from "react-notify-toast";

const systemService = new SystemService();

function DatabasePage() {

  const [sizes, setSizes] = useState();
  const [revision, setRevision] = useState(new Date());

  useEffect(() => {
    setSizes(null);
    systemService.getDatabaseGlobalSizes(setSizes);
  }, [revision])

  const onPurge = () => {
    setRevision(new Date());
    notify.show('Data purge request submitted. It can take a while to complete.', 'success');
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-12">
            <h1>Database</h1>
          </div>
        </div>

        <div className="row">
          <div className="col-12">
            <div className="row mt-3">
              <div className="col-xl-12 col-xxl-6">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Global Database Usage"
                                           slim={true}
                                           helpLink="https://go.nzyme.org/retention-time" />

                    <GlobalDatabaseUsageTable sizes={sizes} onPurge={onPurge} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default DatabasePage;