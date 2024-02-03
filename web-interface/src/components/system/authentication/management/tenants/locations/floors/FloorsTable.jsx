import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import numeral from "numeral";
import Paginator from "../../../../../../misc/Paginator";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";

const authenticationMgmtService = new AuthenticationManagementService();

function FloorsTable(props) {

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;
  const locationId = props.locationId;

  const perPage = 20;
  const [page, setPage] = useState(1);

  const [floors, setFloors] = useState(null);

  useEffect(() => {
    setFloors(null);
    authenticationMgmtService.findAllFloorsOfTenantLocation(organizationId, tenantId, locationId, setFloors, perPage, (page-1)*perPage);
  }, [page]);

  if (!floors) {
    return <LoadingSpinner />
  }

  if (floors.floors.length === 0) {
    return (
        <div className="alert alert-info mb-2">
          This location does not have any floors.
        </div>
    )
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Number</th>
            <th>Name</th>
            <th>Placed Taps</th>
          </tr>
          </thead>
          <tbody>
          {floors.floors.map(function (key, i) {
            return (
                <tr key={i}>
                  <td>
                    <td>{numeral(floors.floors[i].number)}</td>
                    <a href="#">
                      {floors.floors[i].name}
                    </a>
                  </td>
                  <td>TODO</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={floors.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default FloorsTable;