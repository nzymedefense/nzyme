import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../../../misc/LoadingSpinner";
import CotIntegrationService from "../../../../../../../services/integrations/CotIntegrationService";
import Paginator from "../../../../../../misc/Paginator";
import moment from "moment";
import numeral from "numeral";
import CotOutputStatus from "./CotOutputStatus";
import ApiRoutes from "../../../../../../../util/ApiRoutes";
import CotConnectionType from "./CotConnectionType";

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
            <th>Type</th>
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
                  <td>
                    <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.TENANTS.INTEGRATIONS.COT.DETAILS(organizationId, tenantId, output.uuid)}>
                      {output.name}
                    </a>
                  </td>
                  <td>{output.address}:{output.port}</td>
                  <td><CotConnectionType type={output.connection_type} /></td>
                  <td><CotOutputStatus status={output.status} /></td>
                  <td>{numeral(output.sent_messages).format("0,0")} messages / {numeral(output.sent_bytes).format("0 b")}</td>
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