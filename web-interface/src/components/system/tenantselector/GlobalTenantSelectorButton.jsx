import React, {useContext, useState} from "react";
import {UserContext} from "../../../App";
import GlobalTenantSelectorDialog from "./GlobalTenantSelectorDialog";

export default function GlobalTenantSelectorButton() {

  const user = useContext(UserContext);

  const [visible, setVisible] = useState(false);

  if (!user.is_superadmin && !user.is_orgadmin) {
    // Tenant users don't have a tenant to select, and we'll hide the button entirely.
    return;
  }

  return (
    <React.Fragment>
      <button className="btn btn-outline-secondary"
              title="Select Tenant"
              data-bs-toggle="modal"
              data-bs-target="#global-tenant-selector" >
        <i className="fa-solid fa-building-user" />
      </button>

      <GlobalTenantSelectorDialog show={visible} />
    </React.Fragment>
  )

}