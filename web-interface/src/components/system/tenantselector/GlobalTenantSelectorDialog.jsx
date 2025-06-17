import React from "react";
import GlobalTenantSelectorForm from "./GlobalTenantSelectorForm";

export default function GlobalTenantSelectorDialog() {

  const onSelectionMade = (org, tenant) => {
    console.log("onSelectionMade", org, tenant);
  }

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
            <button type="button" className="btn btn-primary">Select Tenant</button>
          </div>
        </div>
      </div>
    </div>
  )

}