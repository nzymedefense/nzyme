import React, {useContext} from "react";
import Routes from "../../../../../util/ApiRoutes";
import {UserContext} from "../../../../../App";

export default function OrganizationHeader(props) {

  const organization = props.organization;

  const user = useContext(UserContext);

  return (
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
              {user.is_superadmin ?
                  <a className="btn btn-primary"
                     href={Routes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EDIT(organization.id)}>
                    Edit Organization
                  </a> : null}
            </span>
        </div>
      </div>
  )

}