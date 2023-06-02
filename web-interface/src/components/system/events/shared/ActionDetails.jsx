import React from "react";
import moment from "moment";

function ActionDetails(props) {
  const action = props.action;

  return (
      <dl className="mb-0">
        <dt>Type</dt>
        <dd title={action.action_type}>{action.action_type_human_readable}</dd>
        <dt>Created at</dt>
        <dd title={moment(action.created_at).format()}>{moment(action.created_at).fromNow()}</dd>
        <dt>Updated at</dt>
        <dd title={moment(action.updated_at).format()}>{moment(action.updated_at).fromNow()}</dd>
      </dl>
  )
}

export default ActionDetails;