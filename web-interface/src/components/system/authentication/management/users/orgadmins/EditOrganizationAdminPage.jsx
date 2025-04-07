import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import EditUserForm from "../shared/EditUserForm";
import {notify} from "react-notify-toast";
import EditPasswordForm from "../shared/EditPasswordForm";

const authenticationManagementService = new AuthenticationManagementService();

function EditOrganizationAdminPage() {

  const { organizationId } = useParams();
  const { userId } = useParams();

  const [organization, setOrganization] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);
  const [user, setUser] = useState(null);

  const [redirect, setRedirect] = useState(false);

  const onEditDetailsFormSubmitted = function(email, name, disableMfa, callback) {
    authenticationManagementService.editOrganizationAdministrator(organization.id, user.id, name, email, disableMfa,
        () => {
          // Success.
          notify.show('Organization Administrator updated.', 'success');
          setRedirect(true);
          callback();
        }, function (error) {
          console.log(error);
          // Error.
          setErrorMessage(error.response.data.message);
          callback();
        })
  }

  const onEditPasswordFormSubmitted = function(password, callback) {
    authenticationManagementService.editOrganizationAdministratorPassword(organization.id, user.id, password, function() {
      // Success.
      notify.show('Password updated.', 'success');
      setRedirect(true);
      callback();
    })
  }

  useEffect(() => {
    authenticationManagementService.findOrganization(organizationId, setOrganization);
    authenticationManagementService.findOrganizationAdmin(organizationId, userId, setUser);
  }, [userId, organizationId])

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.DETAILS(organizationId, userId)} />
  }

  if (!user || !organization) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-9">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>Authentication &amp; Authorization</a>
                </li>
                <li className="breadcrumb-item">Organizations</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organization.id)}>
                    {organization.name}
                  </a>
                </li>
                <li className="breadcrumb-item">Organization Administrators</li>
                <li className="breadcrumb-item">
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.DETAILS(organizationId, userId)}>
                    {user.email}
                  </a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Edit</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
              <span className="float-end">
                <a className="btn btn-secondary"
                   href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.DETAILS(organizationId, userId)}>
                  Back
                </a>
              </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Organization Administrator &quot;{user.email}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Edit Organization Administrator Details</h3>

                    <EditUserForm
                        email={user.email}
                        name={user.name}
                        disableMfa={user.mfa_disabled}
                        submitText="Edit Organization Administrator"
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

export default EditOrganizationAdminPage;