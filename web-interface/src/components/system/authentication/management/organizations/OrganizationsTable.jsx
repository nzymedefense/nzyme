import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../../util/ApiRoutes";

const authenticationMgmtService = new AuthenticationManagementService();

function OrganizationsTable() {

  const [organizations, setOrganizations] = useState(null);

  useEffect(() => {
    authenticationMgmtService.findAllOrganizations(setOrganizations);
  }, []);

  if (!organizations) {
    return <LoadingSpinner />
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Name</th>
          <th>Tenants</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {organizations.map(function (key, i) {
          return (
              <tr key={"org-" + i}>
                <td>{organizations[i].name}</td>
                <td>{organizations[i].tenants_count}</td>
                <td>
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organizations[i].id)}>
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

export default OrganizationsTable;