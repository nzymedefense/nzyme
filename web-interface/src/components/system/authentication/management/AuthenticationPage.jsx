import React, {useContext} from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import OrganizationsTable from "./organizations/OrganizationsTable";
import GlobalSessions from "./sessions/GlobalSessions";
import SuperAdminTable from "./users/superadmins/SuperAdminTable";
import {Navigate} from "react-router-dom";
import Routes from "../../../../util/ApiRoutes";
import {UserContext} from "../../../../App";

function AuthenticationPage() {

  const user = useContext(UserContext);

  // Send an Org Admin directly to their org page because they won't be able to access anything here.
  if (user.is_orgadmin) {
    return <Navigate to={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(user.organization_id)} />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-10">
            <h1>Authentication &amp; Authorization</h1>
          </div>

          <div className="col-md-2">
            <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SETTINGS} className="btn btn-primary float-end">
              Settings
            </a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Organizations</h3>

                    <OrganizationsTable />

                    <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.CREATE}
                       className="btn btn-sm btn-secondary">
                      Create Organization
                    </a>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Super Administrators</h3>

                    <SuperAdminTable />

                    <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.CREATE}
                       className="btn btn-sm btn-secondary">
                      Create Super Administrator
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>All Active Sessions</h3>

                <GlobalSessions />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default AuthenticationPage;