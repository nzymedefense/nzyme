import React from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import OrganizationsTable from "./organizations/OrganizationsTable";
import GlobalSessions from "./sessions/GlobalSessions";
import SuperAdminTable from "./users/superadmins/SuperAdminTable";

function AuthenticationPage() {

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Authentication &amp; Authorization</h1>
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

                    <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.CREATE} className="btn btn-sm btn-primary">
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

                    <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.CREATE} className="btn btn-sm btn-primary">
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