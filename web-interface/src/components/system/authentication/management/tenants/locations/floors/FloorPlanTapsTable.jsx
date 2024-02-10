import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../../../services/AuthenticationManagementService";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import moment from "moment/moment";
import Paginator from "../../../../../../misc/Paginator";

const authenticationMgmtService = new AuthenticationManagementService();

function FloorPlanTapsTable(props) {

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;
  const floorId = props.floorId;
  const onTapPlaced = props.onTapPlaced;
  const onRemoveTap = props.onRemoveTap;

  const perPage = 20;
  const [page, setPage] = useState(1);

  const [taps, setTaps] = useState(null);

  useEffect(() => {
    setTaps(null);
    authenticationMgmtService.findAllTapPermissions(organizationId, tenantId, setTaps, perPage, (page-1)*perPage);
  }, [page]);

  const placementStatus = (tap) => {
    if (tap.is_placed_on_map) {
      if (tap.floor_id === floorId) {
        return {
          status: <span className="badge bg-success">Placed On This Floor</span>,
          action: <a href="#" onClick={(e) => {e.preventDefault(); onRemoveTap(tap.uuid)}}>Remove From This Floor</a>
        }
      } else {
        return {
          status: <span className="badge bg-secondary">Placed On Other Location/Floor</span>,
          action: <span className="text-muted">n/a</span>
        }
      }

    } else {
      return {
        status: <span className="badge bg-warning">Not Placed Anywhere</span>,
        action: <a href="#" onClick={(e) => {e.preventDefault();onTapPlaced(tap)}}>Place On This Floor</a>
      }
    }
  }

  if (!taps) {
    return <LoadingSpinner/>
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
            <th>Placement Status</th>
            <th>Action</th>
          </tr>
          </thead>
          <tbody>
          {taps.taps.map(function (key, i) {
            return (
                <tr key={"tap-" + i}>
                  <td>{taps.taps[i].name}</td>
                  <td>{taps.taps[i].last_report ? moment(taps.taps[i].last_report).format() : <span className="text-warning">None</span>}</td>
                  <td>{placementStatus(taps.taps[i]).status}</td>
                  <td>{placementStatus(taps.taps[i]).action}</td>
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