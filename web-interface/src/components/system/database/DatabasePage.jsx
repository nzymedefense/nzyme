import React, {useEffect, useState} from "react";
import SystemService from "../../../services/SystemService";
import DatabaseSummary from "./DatabaseSummary";
import RetentionTimesConfiguration from "./RetentionTimesConfiguration";
import CardTitleWithControls from "../../shared/CardTitleWithControls";

const systemService = new SystemService();

function DatabasePage() {

  const [summary, setSummary] = useState();

  useEffect(() => {
    systemService.getDatabaseSummary(setSummary)
  }, [])

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Database</h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="row mt-3">
              <div className="col-md-6">
                <div className="card">
                  <div className="card-body">
                    <h3>Database &amp; Table Sizes</h3>

                    <DatabaseSummary summary={summary} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="row mt-3">
              <div className="col-md-6">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Data Retention Configuration"
                                           slim={true}
                                           helpLink="https://go.nzyme.org/retention-time" />

                    <RetentionTimesConfiguration />
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