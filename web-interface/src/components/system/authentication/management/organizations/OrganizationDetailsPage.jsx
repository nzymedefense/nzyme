import React, {useContext, useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import Routes from "../../../../../util/ApiRoutes";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import {notify} from "react-notify-toast";
import TenantsTable from "../tenants/TenantsTable";
import ApiRoutes from "../../../../../util/ApiRoutes";
import OrganizationSessions from "../sessions/OrganizationSessions";
import OrganizationAdminTable from "../users/orgadmins/OrganizationAdminTable";
import {UserContext} from "../../../../../App";
import WithExactRole from "../../../../misc/WithExactRole";

const authenticationManagementService = new AuthenticationManagementService();

function OrganizationDetailsPage() {

  const user = useContext(UserContext);

  const { organizationId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [redirect, setRedirect] = useState(false);

  const deleteOrganization = function() {
    if (!confirm("Really delete organization?")) {
      return;
    }

    authenticationManagementService.deleteOrganization(organizationId, function() {
      setRedirect(true);
      notify.show('Organization deleted.', 'success');
    });
  }

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
  }, [organizationId])

  if (redirect) {
    return <Navigate to={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX} />
  }

  if (!organization) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-9">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">
                  <a href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication &amp; Authorization</a>
                </li>
                <li className="breadcrumb-item">Organizations</li>
                <li className="breadcrumb-item active" aria-current="page">{organization.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              { user.is_superadmin ?
                  <a className="btn btn-secondary" href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Back</a>
                  : null }{' '}

              { user.is_superadmin ?
              <a className="btn btn-primary" href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EDIT(organization.id)}>
                Edit Organization
              </a> : null }
            </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Organization &quot;{organization.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Description</h3>

                    <p className="mb-0">
                      {organization.description}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Tenants</h3>

                    <p className="mb-2">
                      The following tenants are part of this organization.
                    </p>

                    <TenantsTable organizationId={organization.id} />

                    <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.CREATE(organization.id)}
                       className="btn btn-sm btn-secondary">
                      Create Tenant
                    </a>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Organization Administrators</h3>

                    <OrganizationAdminTable organization={organization} />

                    <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.CREATE(organization.id)}
                       className="btn btn-sm btn-secondary">
                      Create Organization Administrator
                    </a>
                  </div>
                </div>
              </div>
            </div>

            <WithExactRole role="SUPERADMIN">
              <div className="row mt-3">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Events &amp; Actions</h3>

                      <p>
                        Events, such as system notifications or detection alerts, within this organization, have the
                        ability to trigger actions, which are managed on the events & action pages.
                      </p>

                      <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS.INDEX(organization.id)}
                         className="btn btn-secondary btn-sm">
                        Manage Organization Events &amp; Actions
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </WithExactRole>
          </div>

          <div className="col-md-6">
            { user.is_superadmin ?
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Delete Organization</h3>

                    <p>
                      You can only delete an organization if it has no tenants and if it is not the last remaining one.
                    </p>

                    <button className="btn btn-sm btn-danger" disabled={!organization.is_deletable} onClick={deleteOrganization}>
                      Delete Organization
                    </button>
                  </div>
                </div>
              </div>
            </div>
            : null }
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>All Active Sessions of Organization</h3>

                <OrganizationSessions organizationId={organization.id} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default OrganizationDetailsPage;