import React, {useState} from "react";
import TenantUserPermissionRow from "./TenantUserPermissionRow";
import {notify} from "react-notify-toast";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";

const authenticationManagementService = new AuthenticationManagementService();

function TenantUserPermissions(props) {

  const allPermissions = props.allPermissions;
  const user = props.user;

  const [userPermissions, setUserPermissions] = useState(user.permissions);

  const [formSubmitting, setFormSubmitting] = useState(false);

  const onPermissionChange = function(e, permissionId) {
    let newPermissions = [...userPermissions];

    if (e.target.checked) {
      // Add permission.
      newPermissions.push(permissionId);
    } else {
      // Remove permission.
      newPermissions = newPermissions.filter(item => item !== permissionId)
    }

    setUserPermissions(newPermissions);
  }

  const onSubmit = function() {
    setFormSubmitting(true);

    authenticationManagementService.editUserOfTenantPermissions(user.organization_id,
        user.tenant_id, user.id, userPermissions, function() {
          setFormSubmitting(false);
          notify.show('Permissions of user updated.', 'success');
        }, function() {
          setFormSubmitting(false);
          notify.show('Could not update permissions of user.', 'error');
        });
  }

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
                <TenantUserPermissionRow key={"userpermission-" + i}
                                         permission={permission}
                                         active={userPermissions.includes(permission.id)}
                                         onChange={onPermissionChange} />
            )
          })}
          </tbody>
        </table>

        <button className="btn btn-sm btn-secondary" onClick={onSubmit}>
          {formSubmitting ? "Please wait ..." : "Update Feature Permissions" }
        </button>
      </React.Fragment>
  )

}

export default TenantUserPermissions;