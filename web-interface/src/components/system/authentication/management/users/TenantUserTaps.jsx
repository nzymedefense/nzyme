import React, {useState} from "react";
import TenantUserTapRow from "./TenantUserTapRow";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import {notify} from "react-notify-toast";

const authenticationManagementService = new AuthenticationManagementService();

function TenantUserTaps(props) {

  const taps = props.taps;
  const user = props.user;
  const onTapsPermissionsUpdated = props.onTapPermissionsUpdated;

  const [allowAccessAllTaps, setAllowAccessAllTaps] = useState(user.allow_access_all_tenant_taps);
  const [tapPermissions, setTapPermissions] = useState(user.tap_permissions);

  const [formSubmitting, setFormSubmitting] = useState(false);

  const toggleAllowAccessAllTaps = function() {
    setAllowAccessAllTaps(!allowAccessAllTaps);
  }

  const toggleTapPermission = function(e, tapUuid) {
    let newPermissions = [...tapPermissions];

    if (e.target.checked) {
      // Add permission.
      newPermissions.push(tapUuid);
    } else {
      // Remove permission.
      newPermissions = newPermissions.filter(item => item !== tapUuid)
    }

    setTapPermissions(newPermissions);
  }

  const onSubmit = function() {
    setFormSubmitting(true);

    authenticationManagementService.editUserOfTenantTapPermissions(user.organization_id,
        user.tenant_id, user.id, allowAccessAllTaps, tapPermissions, function() {
          setFormSubmitting(false);
          notify.show('Tap permissions of user updated.', 'success');
          onTapsPermissionsUpdated();
        }, function() {
          setFormSubmitting(false);
          notify.show('Could not update tap permissions of user.', 'error');
        });
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th style={{width: 35}}>&nbsp;</th>
            <th>Tap Name</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td style={{textAlign: "center"}}>
              <input type="checkbox" checked={allowAccessAllTaps} onChange={toggleAllowAccessAllTaps} />
            </td>
            <td><strong>Allow access to all taps of tenant</strong></td>
          </tr>
          {Object.values(taps.taps).map(function (tap, i) {
            return <TenantUserTapRow key={"tapperm-" + i}
                                     tap={tap}
                                     tapPermissions={tapPermissions}
                                     allowAccessAllTaps={allowAccessAllTaps}
                                     onChange={toggleTapPermission} />
          })}
          </tbody>
        </table>

        <button className="btn btn-sm btn-secondary" disabled={formSubmitting} onClick={onSubmit}>
          {formSubmitting ? "Please wait ..." : "Update Tap Permissions" }
        </button>
      </React.Fragment>
  )

}

export default TenantUserTaps;