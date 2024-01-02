import React from "react";

import moment from "moment";
import LoadingSpinner from "../../misc/LoadingSpinner";

function RestricedSubstringsTable(props) {

  const substrings = props.substrings;
  const onDeleteSubstring = props.onDeleteSubstring;

  if (!substrings) {
    return <LoadingSpinner />
  }

  if (substrings.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          No restricted substrings configured yet.
        </div>
    )
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Substring</th>
          <th>Created At</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {substrings.map((rs, i) => {
          return (
              <tr key={i}>
                <td>{rs.substring}</td>
                <td title={moment(rs.created_at).format()}>{moment(rs.created_at).fromNow()}</td>
                <td><a href="#" onClick={(e) => onDeleteSubstring(e, rs.uuid)}>Delete</a></td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}

export default RestricedSubstringsTable;