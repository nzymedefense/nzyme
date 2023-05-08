import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import SessionsTable from "./SessionsTable";

const authenticationMgmtService = new AuthenticationManagementService();

function OrganizationSessions(props) {

  const organizationId = props.organizationId;

  const [sessions, setSessions] = useState(null);
  const [revision, setRevision] = useState(0);

  const [page, setPage] = useState(1);
  const perPage = 20;

  const fetchData = function() {
    authenticationMgmtService.findSessionsOfOrganization(setSessions, organizationId, perPage, (page-1)*perPage)
  }

  useEffect(() => {
    setSessions(null);
    fetchData();

    const x = setInterval(() => {
      fetchData();
    }, 5000);

    return () => {
      clearInterval(x);
    };
  }, [page, revision, organizationId]);

  return <SessionsTable sessions={sessions}
                        perPage={perPage}
                        page={page}
                        setPage={setPage}
                        revision={revision}
                        setRevision={setRevision} />

}

export default OrganizationSessions;