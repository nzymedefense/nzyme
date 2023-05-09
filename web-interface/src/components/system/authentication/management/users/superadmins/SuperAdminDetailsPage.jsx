import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import moment from "moment";
import {notify} from "react-notify-toast";
import LastUserActivity from "../shared/LastUserActivity";

const authenticationManagementService = new AuthenticationManagementService();

function SuperAdminDetailsPage() {

  const { userId } = useParams();

  const [user, setUser] = useState(null);
  const [isDeletable, setIsDeletable] = useState(null);

  const [redirect, setRedirect] = useState(false);

  const deleteSuperAdmin = function() {
    if (!confirm("Really delete super administrator?")) {
      return;
    }

    authenticationManagementService.deleteSuperAdmin(userId, function() {
      setRedirect(true);
      notify.show('Super administrator deleted.', 'success');
    });
  }

  const resetMfa = function() {
    if (!confirm("Really reset MFA credentials for this user?")) {
      return;
    }

    authenticationManagementService.resetMFAOfSuperAdmin(userId, function() {
      notify.show('MFA successfully reset.', 'success');
    });
  }

  useEffect(() => {
    authenticationManagementService.findSuperAdmin(userId, setUser, setIsDeletable);
  }, [userId])

  if (redirect) {
    return <Navigate to={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX} />
  }

  if (!user) {
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
                <li className="breadcrumb-item">Super Administrators</li>
                <li className="breadcrumb-item active" aria-current="page">{user.email}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-3">
            <span className="float-end">
              <a className="btn btn-secondary" href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}>
                Back
              </a>{' '}
              <a className="btn btn-primary" href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.EDIT(user.id)}>
                Edit User
              </a>
            </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Super Administrator &quot;{user.email}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>User Details</h3>

                    <dl className="mb-0">
                      <dt>Email Address / Username</dt>
                      <dd>{user.email}</dd>

                      <dt>Full Name</dt>
                      <dd>{user.name}</dd>

                      <dt>Created At</dt>
                      <dd title={moment(user.created_at).format()}>
                        {moment(user.created_at).fromNow()}
                      </dd>

                      <dt>Updated At</dt>
                      <dd title={moment(user.updated_at).format()}>
                        {moment(user.updated_at).fromNow()}
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Last Activity</h3>

                    <LastUserActivity
                        timestamp={user.last_activity}
                        remoteAddress={user.last_remote_ip}
                        remoteCountry={user.last_geo_country}
                        remoteCity={user.last_geo_city}
                        remoteAsn={user.last_geo_asn} />
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
                    <h3>Delete User</h3>

                    <p>
                      Note that you cannot delete yourself and you also cannot delete the last remaining super
                      administrator of this nzyme system.
                    </p>

                    <button className="btn btn-sm btn-danger" onClick={deleteSuperAdmin} disabled={!isDeletable}>
                      Delete User
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Reset MFA</h3>

                    <p>
                      The user will be logged out and prompted to set up new MFA credentials after logging back in.
                    </p>

                    <button className="btn btn-sm btn-warning" onClick={resetMfa}>
                      Reset MFA Credentials
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default SuperAdminDetailsPage;