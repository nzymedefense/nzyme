import React from "react";
import moment from "moment";
import ApiRoutes from "../../../../../../util/ApiRoutes";

function SuperAdminTableRow(props) {

  const user = props.user;

  return (
      <tr>
        <td>
          <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.DETAILS(user.id)}>
          {user.name}
          </a>
        </td>
        <td>{user.email}</td>
        <td title={user.last_activity ? moment(user.last_activity).format() : "None"}>
          {user.last_activity ? moment(user.last_activity).fromNow() : "None"}
        </td>
      </tr>
  )

}

export default SuperAdminTableRow;
