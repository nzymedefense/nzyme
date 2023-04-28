import React from "react";
import Paginator from "../../../../misc/Paginator";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import moment from "moment";

function SessionsTable(props) {

  const sessions = props.sessions;
  const perPage = props.perPage;
  const page = props.page;
  const setPage = props.setPage;

  if (!sessions) {
    return <LoadingSpinner />
  }

  if (sessions.sessions.length === 0) {
    return (
        <div className="alert alert-info">
          No current sessions.
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
            <th>Last Activity</th>
          </tr>
          </thead>
          <tbody>
          {sessions.sessions.map((s, i) => {
            return (
                <tr key={"session-" + i}>
                  <td>{s.user_email}</td>
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
                  <td title={moment(s.last_activity).format()}>
                    {moment(s.last_activity).fromNow()}
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