import React from "react";

function TenantUserPermissionRow(props) {

  const permission = props.permission;
  const active = props.active;

  return (
      <React.Fragment>
        <tr>
          <td style={{textAlign: "center"}}>
            <input type="checkbox" checked={active} />
          </td>
          <td>
            <strong>{permission.name}</strong>
            <br />
            <span style={{marginTop: 3, display: "block"}}>{permission.description}</span>

            {permission.respects_tap_scope ?
                null
                : <span className="text-warning"><i className="fa fa-warning"></i> This permission does not consider tap scope.</span>}
          </td>
        </tr>
      </React.Fragment>
  )

}

export default TenantUserPermissionRow;