import React, {useEffect, useState} from "react";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import SessionsTable from "./SessionsTable";


const authenticationMgmtService = new AuthenticationManagementService();

function GlobalSessions() {

  const [sessions, setSessions] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 20;

  useEffect(() => {
    authenticationMgmtService.findAllSessions(setSessions, perPage, (page-1)*perPage)
  }, [page]);

  return <SessionsTable sessions={sessions} perPage={perPage} page={page} setPage={setPage} />

}

export default GlobalSessions;