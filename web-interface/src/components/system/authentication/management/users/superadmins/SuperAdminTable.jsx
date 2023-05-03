import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../../services/AuthenticationManagementService";
import SuperAdminTableRow from "./SuperAdminTableRow";

const authenticationMgmtService = new AuthenticationManagementService();

function SuperAdminTable() {

  const [users, setUsers] = useState(null);

  useEffect(() => {
    authenticationMgmtService.findAllSuperAdmins(setUsers);
  }, []);

  if (!users) {
    return <LoadingSpinner />
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Last Activity</th>
        </tr>
        </thead>
        <tbody>
        {users.map(function (key, i) {
          return <SuperAdminTableRow key={"superadmins-" + i} user={users[i]} />
        })}
        </tbody>
      </table>
  )

}

export default SuperAdminTable;