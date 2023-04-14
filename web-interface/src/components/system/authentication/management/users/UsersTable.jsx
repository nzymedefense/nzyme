import React from "react";

function UsersTable() {

  return (
      <div className="alert alert-info">
        This tenant does not have any users.
      </div>
  )

  /*return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Email</th>
          <th>Name</th>
          <th>Role</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        </tbody>
      </table>
  )*/

}

export default UsersTable;