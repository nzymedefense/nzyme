import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import UsersTableRow from "./UsersTableRow";

const authenticationMgmtService = new AuthenticationManagementService();

function UsersTable(props) {

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;

  const [users, setUsers] = useState(null);

  useEffect(() => {
    authenticationMgmtService.findAllUsersOfTenant(organizationId, tenantId, setUsers);
  }, []);

  if (users === null || users === undefined) {
    return <LoadingSpinner />
  }

  if (users.length === 0) {
    return (
        <div className="alert alert-info">
          This tenant does not have any users.
        </div>
    )
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Role</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {users.map(function (key, i) {
          return <UsersTableRow key={"tenantusers-" + i} user={users[i]} />
        })}
        </tbody>
      </table>
  )

}

export default UsersTable;