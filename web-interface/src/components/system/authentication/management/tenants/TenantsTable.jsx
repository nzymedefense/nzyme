import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../../util/ApiRoutes";

const authenticationMgmtService = new AuthenticationManagementService();

function TenantsTable(props) {

  const organizationId = props.organizationId;

  const [tenants, setTenants] = useState(null);

  useEffect(() => {
    authenticationMgmtService.findAllTenantsOfOrganization(organizationId, setTenants);
  }, []);

  if (tenants === null || tenants === undefined) {
    return <LoadingSpinner />
  }

  if (tenants.length === 0) {
    return (
        <div className="alert alert-info mb-2">
          This organization does not have any tenants.
        </div>
    )
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Name</th>
          <th>Users</th>
          <th>Taps</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {tenants.map(function (key, i) {
          return (
              <tr key={"tenant-" + i}>
                <td>{tenants[i].name}</td>
                <td>{tenants[i].users_count}</td>
                <td>{tenants[i].taps_count}</td>
                <td>
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.DETAILS(tenants[i].organization_id, tenants[i].id)}>
                    Details
                  </a>
                </td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}

export default TenantsTable;