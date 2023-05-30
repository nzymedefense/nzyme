import React from "react";
import ActionsTable from "./ActionsTable";
import ApiRoutes from "../../../../../../util/ApiRoutes";

function Actions(props) {

  const organizationId = props.organizationId;

  return (
      <React.Fragment>
        <ActionsTable />

        <a className="btn btn-secondary btn-sm"
           href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ACTIONS.CREATE(organizationId)}>
          Create Action
        </a>
      </React.Fragment>
  )

}

export default Actions;