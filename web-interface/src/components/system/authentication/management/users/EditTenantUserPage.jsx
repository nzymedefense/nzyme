import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import {notify} from "react-notify-toast";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import Routes from "../../../../../util/ApiRoutes";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../../util/ApiRoutes";
import EditUserForm from "./shared/EditUserForm";
import EditPasswordForm from "./shared/EditPasswordForm";

const authenticationManagementService = new AuthenticationManagementService();

function EditTenantUserPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();
  const { userId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);
  const [user, setUser] = useState(null);

  const [redirect, setRedirect] = useState(false);

  const onEditDetailsFormSubmitted = function (email, name, callback) {
    authenticationManagementService.editUserOfTenant(organization.id, tenant.id, user.id, name, email, function() {
      // Success.
      notify.show('User updated.', 'success');
      setRedirect(true);
      callback();
    }, function (error) {
      // Error.
      setErrorMessage(error.response.data.message);
      callback();
    })
  }

  const onEditPasswordFormSubmitted = function (password, callback) {
    authenticationManagementService.editUserOfTenantPassword(organization.id, tenant.id, user.id, password, function() {
      // Success.
      notify.show('Password updated.', 'success');
      setRedirect(true);
      callback();
    })
  }

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findTenantOfOrganization(organizationId, tenantId, setTenant);
    authenticationManagementService.findUserOfTenant(organizationId, tenantId, userId, setUser);
  }, [organizationId, tenantId])

  if (redirect) {
    return <Navigate to={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.DETAILS(organization.id, tenant.id, user.id)} />
  }

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
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.TENANTS_PAGE(organization.id)}>
                  Tenants
                </a>
              </li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(organization.id, tenant.id)}>
                {tenant.name}
                </a>
              </li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.USERS_PAGE(organization.id, tenant.id)}>
                  Users
                </a>
              </li>
              <li className="breadcrumb-item">
                <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.DETAILS(organization.id, tenant.id, user.id)}>
                  {user.email}
                </a>
              </li>
              <li className="breadcrumb-item active"aria-current="page">Edit</li>
            </ol>
          </nav>
        </div>

        <div className="col-md-3">
              <span className="float-end">
                <a className="btn btn-secondary"
                   href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.DETAILS(organization.id, tenant.id, user.id)}>
                  Back
                </a>
              </span>
        </div>
      </div>

      <div className="row">
        <div className="col-md-12">
          <h1>User &quot;{user.email}&quot;</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-8">
          <div className="row">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <h3>Edit User Details</h3>

                  <EditUserForm
                      email={user.email}
                      name={user.name}
                      submitText="Edit User"
                      errorMessage={errorMessage}
                      onClick={onEditDetailsFormSubmitted} />
                </div>
              </div>
            </div>
          </div>

          <div className="row mt-3">
            <div className="col-md-12">
              <div className="card">
                <div className="card-body">
                  <h3>Change Password</h3>

                  <p>
                    Changing the password will log out the user and prompt them to log in again with the
                    new password.
                  </p>

                  <EditPasswordForm
                      submitText="Change Password"
                      onClick={onEditPasswordFormSubmitted} />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>


    </React.Fragment>
  )

}

export default EditTenantUserPage;