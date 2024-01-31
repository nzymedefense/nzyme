import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import Routes from "../../../../../util/ApiRoutes";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../../util/ApiRoutes";
import {notify} from "react-notify-toast";
import TenantUsersTable from "../users/TenantUsersTable";
import TapPermissionsTable from "../taps/TapPermissionsTable";
import TenantSessions from "../sessions/TenantSessions";
import LocationsTable from "./locations/LocationsTable";

const authenticationManagementService = new AuthenticationManagementService();

function TenantDetailsPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [redirect, setRedirect] = useState(false);

  const deleteTenant = function() {
    if (!confirm("Really delete tenant?")) {
      return;
    }

    authenticationManagementService.deleteTenantOfOrganization(organizationId, tenantId, function() {
      setRedirect(true);
      notify.show('Tenant deleted.', 'success');
    });
  }

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
  }, [organizationId, tenantId])

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)} />
  }

  if (!organization || !tenant) {
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
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                    {organization.name}
                  </a>
                </li>
                <li className="breadcrumb-item">Tenants</li>
                <li className="breadcrumb-item active" aria-current="page">{tenant.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary" href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                Back
              </a>{' '}
              <a className="btn btn-primary" href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.EDIT(organization.id, tenant.id)}>
                Edit Tenant
              </a>
            </span>
          </div>

          <div className="col-md-12">
            <h1>Tenant &quot;{tenant.name}&quot;</h1>
          </div>

          <div className="row mt-3">
            <div className="col-md-8">
              <div className="row">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Description</h3>

                      <p className="mb-0">
                        {tenant.description}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="row mt-3">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Users</h3>

                      <TenantUsersTable organizationId={organization.id} tenantId={tenant.id}/>

                      <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.CREATE(organization.id, tenant.id)}
                         className="btn btn-sm btn-secondary">
                        Create User
                      </a>
                    </div>
                  </div>
                </div>
              </div>

              <div className="row mt-3">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Taps</h3>

                      <TapPermissionsTable organizationId={organization.id} tenantId={tenant.id} />

                      <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.CREATE(organization.id, tenant.id)}
                         className="btn btn-sm btn-secondary">
                        Create Tap
                      </a>
                    </div>
                  </div>
                </div>
              </div>

              <div className="row mt-3">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Locations</h3>

                      <p>Physical locations that taps are deployed at. Used for floor plans across the product.</p>

                      <LocationsTable organizationId={organization.id} tenantId={tenant.id} />

                      <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.LOCATIONS.CREATE(organization.id, tenant.id)}
                         className="btn btn-sm btn-secondary">
                        Create Location
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="row">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Delete Tenant</h3>

                      <p>
                        You can only delete a tenant if it has no users and no taps.
                      </p>

                      <button className="btn btn-sm btn-danger" disabled={!tenant.is_deletable} onClick={deleteTenant}>
                        Delete Tenant
                      </button>
                    </div>
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
                <h3>All Active Sessions of Tenant</h3>

                <TenantSessions organizationId={organization.id} tenantId={tenant.id} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default TenantDetailsPage;