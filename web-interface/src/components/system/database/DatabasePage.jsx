import React, {useEffect, useState} from "react";
import SystemService from "../../../services/SystemService";
import DatabaseSummary from "./DatabaseSummary";

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
            <h1>Events &amp; Actions</h1>
          </div>
        </div>

        <div className="alert alert-danger mb-0 mt-2">
          This is an early alpha page that should not be taken as an example of what future functionality will look like.
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
      </React.Fragment>
  )

}

export default DatabasePage;