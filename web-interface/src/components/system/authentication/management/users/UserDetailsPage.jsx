import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import Routes from "../../../../../util/ApiRoutes";
import ApiRoutes from "../../../../../util/ApiRoutes";

const authenticationManagementService = new AuthenticationManagementService();

function UserDetailsPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();
  const { userId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [user, setUser] = useState(null);

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
    authenticationManagementService.findUserOfTenant(organizationId, tenantId, userId, setUser);
  }, [organizationId, tenantId])

  if (!organization || !tenant || !user) {
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
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                    {tenant.name}
                  </a>
                </li>
                <li className="breadcrumb-item">Users</li>
                <li className="breadcrumb-item active" aria-current="page">{user.email}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary"
                 href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.DETAILS(organization.id, tenant.id, user.id)}>
                Back
              </a>{' '}
            </span>
          </div>

          <div className="col-md-12">
            <h1>User &quot;{user.email}&quot;</h1>
          </div>
        </div>
      </React.Fragment>
  )

}

export default UserDetailsPage;