import React from "react";
import ApiRoutes from "../../../../util/ApiRoutes";

function UserDetailsAdminLink(props) {

  const userId = props.id;
  const email = props.email;
  const organizationId = props.organizationId;
  const tenantId = props.tenantId;

  if (!organizationId && !tenantId) {
    // Superadmin.
    return <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.SUPERADMINS.DETAILS(userId)}>
      {email}
    </a>
  }

  if (organizationId && !tenantId) {
    // Org admin.
    return <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS.DETAILS(organizationId, userId)}>
      {email}
    </a>  }

  // Tenant User.
  return <a href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.USERS.DETAILS(organizationId, tenantId, userId)}>
    {email}
  </a>

}

export default UserDetailsAdminLink;