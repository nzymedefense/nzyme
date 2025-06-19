import React, {useContext, useState} from "react";
import GlobalTenantSelectorForm from "./GlobalTenantSelectorForm";

import Store from "../../../util/Store";
import {AppContext} from "../../../App";

export default function GlobalTenantSelectorDialog() {

  const app = useContext(AppContext);

  const [selectedOrganization, setSelectedOrganization] = useState(null);
  const [selectedTenant, setSelectedTenant] = useState(false);

  const onSelectionMade = (organization, tenant) => {
    setSelectedOrganization(organization);
    setSelectedTenant(tenant);
  }

  const save = () => {
    Store.set("selected_organization", selectedOrganization);
    Store.set("selected_tenant", selectedTenant);

    app.setRevision(new Date());
  }

  // IMPORTANT: The forced selector that shows when no org/tenant selection has been made has its own callbacks.

  return (
    <div className="modal" id="global-tenant-selector">
      <div className="modal-dialog">
        <div className="modal-content">
          <div className="modal-header">
            <h1 className="modal-title fs-5" id="exampleModalLabel">Tenant Selector</h1>
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div className="modal-body">
            <p className="text-muted">
              Select the tenant you want to use the web interface as. This does not affect your user&apos;s privileges
              but is required to access tenant-specific dialogs and pages.
            </p>

            <GlobalTenantSelectorForm onSelectionMade={onSelectionMade} />
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Close</button>

            {selectedOrganization && selectedTenant ?
              <button type="button" className="btn btn-primary" data-bs-dismiss="modal" onClick={save}>Select Tenant</button>
            : <button type="button" className="btn btn-primary" disabled={true}>Select Tenant</button> }
          </div>
        </div>
      </div>
    </div>
  )

}