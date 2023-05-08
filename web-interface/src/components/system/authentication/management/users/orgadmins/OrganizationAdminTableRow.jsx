import React from "react";
import moment from "moment";
import ApiRoutes from "../../../../../../util/ApiRoutes";

function OrganizationAdminTableRow(props) {

  const organization = props.organization;
  const user = props.user;

  return (
      <tr>
        <td>
          <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.DETAILS(organization.id, user.id)}>
            {user.name}
          </a>
        </td>
        <td>{user.email}</td>
        <td>{user.role ? user.role : <span className="text-warning">No Role</span>}</td>
        <td title={user.last_activity ? moment(user.last_activity).format() : "None"}>
          {user.last_activity ? moment(user.last_activity).fromNow() : "None"}
        </td>
      </tr>
  )

}

export default OrganizationAdminTableRow;
