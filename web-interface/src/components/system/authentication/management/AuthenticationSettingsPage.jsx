import React from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import AuthenticationSettings from "./AuthenticationSettings";

function AuthenticationSettingsPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-8">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication</a></li>
                <li className="breadcrumb-item active" aria-current="page">Settings</li>
              </ol>
            </nav>
          </div>

          <div className="col-4">
            <span className="float-end">
              <a className="btn btn-primary" href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Back</a>
            </span>
          </div>

          <div className="col-12">
            <h1>Authentication Settings</h1>
          </div>
        </div>

        <div className="row mt-2">
          <div className="col-xl-12 col-xxl-6">
            <div className="card">
              <div className="card-body">
                <h3>Configuration</h3>

                <div className="alert alert-warning">
                  <strong>These settings apply to super administrators and organization administrators.</strong> Each
                  tenant has individual authentication settings.
                </div>

                <AuthenticationSettings />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default AuthenticationSettingsPage;