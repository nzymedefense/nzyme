import React, {useEffect, useState} from "react";
import OrganizationAndTenantSelector from "../../shared/OrganizationAndTenantSelector";
import SelectedOrganizationAndTenant from "../../shared/SelectedOrganizationAndTenant";
import Paginator from "../../misc/Paginator";
import ContextService from "../../../services/ContextService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Subsystem from "../../misc/Subsystem";

const contextService = new ContextService();

function MacAddressContextTable() {

  const [organizationId, setOrganizationId] = useState(null);
  const [tenantId, setTenantId] = useState(null);

  const [context, setContext] = useState(null);

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    if (organizationId && tenantId) {
      contextService.findAllMacAddressContext(organizationId, tenantId, setContext, perPage, (page - 1) * perPage);
    }
  }, [page, organizationId, tenantId]);

  const onOrganizationChange = (uuid) => {
    setOrganizationId(uuid);
  }

  const onTenantChange = (uuid) => {
    setTenantId(uuid);
  }

  const resetTenantAndOrganization = () => {
    setOrganizationId(null);
    setTenantId(null);
  }

  if (!organizationId || !tenantId) {
    return <OrganizationAndTenantSelector onOrganizationChange={onOrganizationChange} onTenantChange={onTenantChange} />
  }

  if (!context) {
    return <LoadingSpinner />
  }

  if (context.total === 0) {
    return (
        <div className="alert alert-info mb-0">No context has been created yet.</div>
    )
  }

  return (
      <React.Fragment>
        <SelectedOrganizationAndTenant
            organizationId={organizationId}
            tenantId={tenantId}
            onReset={resetTenantAndOrganization} />

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>MAC Address</th>
            <th>Name</th>
            <th>Description</th>
          </tr>
          </thead>
          <tbody>
          {context.mac_addresses.map((m, i) => {
            return (
                <tr key={i}>
                  <td><a href="#">{m.mac_address}</a></td>
                  <td>{m.name}</td>
                  <td>{m.description}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={context.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default MacAddressContextTable;