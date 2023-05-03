import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import TenantUsersTableRow from "./TenantUsersTableRow";

const authenticationMgmtService = new AuthenticationManagementService();

function TenantUsersTable(props) {

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
        <div className="alert alert-info mb-2">
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
          <th>Last Activity</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {users.map(function (key, i) {
          return <TenantUsersTableRow key={"tenantusers-" + i} user={users[i]} />
        })}
        </tbody>
      </table>
  )

}

export default TenantUsersTable;