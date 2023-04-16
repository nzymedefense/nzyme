import React from "react";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import {useParams} from "react-router-dom";

const authenticationMgmtService = new AuthenticationManagementService();

function CreateTapPage() {

  const { organizationId } = useParams();
  const { tenantId } = useParams();

  return <span>create tap</span>

}

export default CreateTapPage;