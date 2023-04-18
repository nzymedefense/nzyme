import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import moment from "moment";
import ApiRoutes from "../../../../../util/ApiRoutes";

const authenticationMgmtService = new AuthenticationManagementService();

function TapPermissionsTable(props) {

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;

  const [taps, setTaps] = useState(null);

  useEffect(() => {
    authenticationMgmtService.findAllTapPermissions(organizationId, tenantId, setTaps);
  }, []);

  if (taps === null || taps === undefined) {
    return <LoadingSpinner />
  }

  if (taps.length === 0) {
    return (
        <div className="alert alert-info mb-2">
          This tenant does not have any taps.
        </div>
    )
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Name</th>
          <th>Last Report</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {taps.map(function (key, i) {
          return (
              <tr key={"tap-" + i}>
                <td>{taps[i].name}</td>
                <td>{taps[i].last_report ? moment(taps[i].last_report).format() : <span className="text-warning">None</span>}</td>
                <td>
                  <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TAPS.DETAILS(
                      organizationId, tenantId, taps[i].uuid)}>
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

export default TapPermissionsTable;