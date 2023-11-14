import React from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import SuperadminSettings from "./SuperadminSettings";

function SuperadminSettingsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-8">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.SYSTEM.AUTHENTICATION.INDEX}>Authentication</a></li>
                <li className="breadcrumb-item active" aria-current="page">Global Super Administrator Settings</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-4">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.SYSTEM.AUTHENTICATION.INDEX}>Back</a>
            </span>
          </div>

          <div className="col-md-12">
            <h1>
              Global Super Administrator Settings
            </h1>
          </div>
        </div>

        <div className="row mt-2">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Configuration</h3>

                <SuperadminSettings />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default SuperadminSettingsPage;