import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import moment from "moment/moment";
import Paginator from "../../../../../../misc/Paginator";

const authenticationMgmtService = new AuthenticationManagementService();

function FloorPlanTapsTable(props) {

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;
  const onTapPlaced = props.onTapPlaced;

  const perPage = 20;
  const [page, setPage] = useState(1);

  const [taps, setTaps] = useState(null);

  useEffect(() => {
    setTaps(null);
    authenticationMgmtService.findAllTapPermissions(organizationId, tenantId, setTaps, perPage, (page-1)*perPage);
  }, [page]);

  if (!taps) {
    return <LoadingSpinner />
  }

  if (taps.taps.length === 0) {
    return (
        <div className="alert alert-info mb-2">
          This tenant does not have any taps.
        </div>
    )
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Name</th>
            <th>Last Report</th>
            <th>Status</th>
          </tr>
          </thead>
          <tbody>
          {taps.taps.map(function (key, i) {
            return (
                <tr key={"tap-" + i}>
                  <td>{taps.taps[i].name}</td>
                  <td>{taps.taps[i].last_report ? moment(taps.taps[i].last_report).format() : <span className="text-warning">None</span>}</td>
                  <td>
                    TODO

                    <a href="#" onClick={(e) => { e.preventDefault(); onTapPlaced(taps.taps[i]); }}>PLACE</a>
                  </td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={taps.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default FloorPlanTapsTable;