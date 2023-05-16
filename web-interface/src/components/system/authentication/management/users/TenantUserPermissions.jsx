import React from "react";
import TenantUserPermissionRow from "./TenantUserPermissionRow";

function TenantUserPermissions(props) {

  const allPermissions = props.allPermissions;
  const user = props.user;

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th style={{width: 35}}>&nbsp;</th>
            <th>Permission Name &amp; Description</th>
          </tr>
          </thead>
          <tbody>
          {allPermissions.map(function (permission, i) {
            return (
                <TenantUserPermissionRow key={"userpermission-" + i} permission={permission} active={true} />
            )
          })}
          </tbody>
        </table>

        <button className="btn btn-sm btn-secondary">Update Feature Permissions</button>
      </React.Fragment>
  )

}

export default TenantUserPermissions;