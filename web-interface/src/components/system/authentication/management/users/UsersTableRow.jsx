import React from "react";

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
          <a href="#">Details</a>
        </td>
      </tr>
  )

}

export default UsersTableRow;
