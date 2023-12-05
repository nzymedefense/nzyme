import React, {useEffect, useState} from "react";
import OrganizationAndTenantSelector from "../../shared/OrganizationAndTenantSelector";
import SelectedOrganizationAndTenant from "../../shared/SelectedOrganizationAndTenant";
import Paginator from "../../misc/Paginator";
import ContextService from "../../../services/ContextService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ApiRoutes from "../../../util/ApiRoutes";

const contextService = new ContextService();

function MacAddressContextTable() {

  const [organizationId, setOrganizationId] = useState(null);
  const [tenantId, setTenantId] = useState(null);

  const [context, setContext] = useState(null);

  const [addressFilter, setAddressFilter] = useState("");
  const [addressFilterRevision, setAddressFilterRevision] = useState(0);

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    if (organizationId && tenantId) {
      contextService.findAllMacAddressContext(organizationId, tenantId, addressFilter, setContext, perPage, (page - 1) * perPage);
    }
  }, [page, organizationId, tenantId, addressFilterRevision]);

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

        <div className="row mb-3">
          <div className="col-xl-12 col-xxl-8">
            <div className="input-group">
              <input type="text" className="form-control" id="macAddress"
                     autocomplete="off"
                     value={addressFilter} onChange={(e) => { setAddressFilter(e.target.value.toUpperCase()) }} />
              <div className="input-group-append">
                <button className="btn btn-outline-secondary"
                        onClick={() => setAddressFilterRevision(prevRev => prevRev + 1)}>
                  Filter MAC Address
                </button>
              </div>
            </div>
          </div>
        </div>

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
                  <td><a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.SHOW(m.uuid, organizationId, tenantId)}>{m.mac_address}</a></td>
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