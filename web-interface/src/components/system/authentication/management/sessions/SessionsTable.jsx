import React from "react";
import Paginator from "../../../../misc/Paginator";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import moment from "moment";
import AuthenticationManagementService from "../../../../../services/AuthenticationManagementService";
import UserDetailsAdminLink from "../../misc/UserDetailsAdminLink";

const authenticationMgmtService = new AuthenticationManagementService();

function SessionsTable(props) {

  const sessions = props.sessions;
  const perPage = props.perPage;
  const page = props.page;
  const setPage = props.setPage;
  const revision = props.revision;
  const setRevision = props.setRevision;

  const invalidateSession = function(e, sessionId) {
    e.preventDefault();

    if (!confirm("Really log out user?")) {
      return;
    }

    authenticationMgmtService.invalidateSession(sessionId, function() {
      setRevision(revision+1);
    });
  }

  if (!sessions) {
    return <LoadingSpinner />
  }

  if (sessions.sessions.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          No active sessions.
        </div>
    )
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Username</th>
            <th>Name</th>
            <th>Remote IP</th>
            <th>Type</th>
            <th>Logged In</th>
            <th>MFA Status</th>
            <th>Last Activity</th>
            <th>&nbsp;</th>
          </tr>
          </thead>
          <tbody>
          {sessions.sessions.map((s, i) => {
            return (
                <tr key={"session-" + i}>
                  <td>
                    <UserDetailsAdminLink id={s.user_id}
                                          email={s.user_email}
                                          organizationId={s.organization_id}
                                          tenantId={s.tenant_id} />
                  </td>
                  <td>{s.user_name}</td>
                  <td>{s.remote_ip}</td>
                  <td>
                    {s.is_superadmin ? "Super Admin" : null}
                    {s.is_orgadmin ? "Organization Admin" : null}
                    {!s.is_superadmin && !s.is_orgadmin ? "User" : null}
                  </td>
                  <td title={moment(s.created_at).format()}>
                    {moment(s.created_at).fromNow()}
                  </td>
                  <td>
                    {s.mfa_valid ? "Passed/Active" : "Pending"}
                  </td>
                  <td title={s.last_activity ? moment(s.last_activity).format() : "None"}>
                    {s.last_activity ? moment(s.last_activity).fromNow() : "None" }
                  </td>
                  <td>
                    <a href="#" onClick={(e) => invalidateSession(e, s.id)}>Force Log Out</a>
                  </td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={sessions.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default SessionsTable;