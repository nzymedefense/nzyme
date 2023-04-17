import React from "react";
import ApiRoutes from "../../../../../util/ApiRoutes";

function UsersTableRow(props) {

  const user = props.user;

  return (
      <tr>
        <td>{user.name}</td>
        <td>{user.email}</td>
        <td>
          {user.role ? user.role : <span className="text-warning">No Role</span>}
        </td>
        <td>
          <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.DETAILS(
              user.organization_id,
              user.tenant_id,
              user.id)}>
            Details
          </a>
        </td>
      </tr>
  )

}

export default UsersTableRow;
