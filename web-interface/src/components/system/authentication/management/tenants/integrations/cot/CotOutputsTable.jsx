import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import CotIntegrationService from "../../../../../../../services/integrations/CotIntegrationService";
import Paginator from "../../../../../../misc/Paginator";
import moment from "moment";
import CotOutputStatus from "./CotOutputStatus";

const cotService = new CotIntegrationService();

export default function CotOutputsTable(props) {

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;

  const [outputs, setOutputs] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 25;

  useEffect(() => {
    setOutputs(null);
    cotService.findAllOutputs(setOutputs, organizationId, tenantId, perPage, (page-1)*perPage)
  }, [organizationId, tenantId, page])

  if (!outputs) {
    return <LoadingSpinner />
  }

  if (outputs.outputs.length === 0) {
    return <div className="alert alert-warning mb-0">No Cursor on Target outputs configured.</div>
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Name</th>
            <th>Address</th>
            <th>Status</th>
            <th>Data Transmitted</th>
            <th>Created At</th>
            <th>Updated At</th>
          </tr>
          </thead>
          <tbody>
          {outputs.outputs.map((output, i) => {
            return (
                <tr key={i}>
                  <td><a href="#">{output.name}</a></td>
                  <td>{output.address}:{output.port}</td>
                  <td><CotOutputStatus status={output.status} /></td>
                  <td>TODO</td>
                  <td title={output.created_at}>{moment(output.created_at).fromNow()}</td>
                  <td title={output.updated_at}>{moment(output.updated_at).fromNow()}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={outputs.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}