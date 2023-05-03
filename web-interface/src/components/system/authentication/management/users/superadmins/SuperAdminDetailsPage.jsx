import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import ApiRoutes from "../../../../../../util/ApiRoutes";
import moment from "moment";

const authenticationManagementService = new AuthenticationManagementService();

function SuperAdminDetailsPage() {

  const { userId } = useParams();

  const [user, setUser] = useState(null);

  useEffect(() => {
    authenticationManagementService.findSuperAdmin(userId, setUser);
  }, [userId])

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
              <a className="btn btn-primary" href="#">
                Edit User TODO
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
          </div>

          <div className="col-md-4">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Delete User</h3>

                    <p>
                      Note that you cannot delete yourself. <strong className="text-danger">TODO IMPLEMENT THIS</strong>
                    </p>

                    <button className="btn btn-sm btn-danger">
                      Delete User TODO
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