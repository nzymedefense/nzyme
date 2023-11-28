import React, {useContext} from "react";
import {UserContext} from "../../App";

function ContextOwnerInformation(props) {

  const context = props.context;

  const user = useContext(UserContext);

  if (user.is_superadmin) {
    return <i className="fa-regular fa-user context-owner-information"
              title={"You are logged in as super administrator. This is the first discovered context, belonging " +
                  "to organization \"" + context.organization_name + "\" and " +
                  "tenant \"" + context.tenant_name + "\"."}></i>
  }

  if (user.is_orgadmin) {
    return <i className="fa-regular fa-user context-owner-information"
              title={"You are logged in as organization administrator. This is the first discovered context, " +
                  "belonging to tenant \"" + context.tenant_name + "\"."}></i>
  }

  return null;

}

export default ContextOwnerInformation;