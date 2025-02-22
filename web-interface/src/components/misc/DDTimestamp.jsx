import React from "react";
import moment from "moment";

export default function DDTimestamp(props) {

  const timestamp = props.timestamp;

  if (!timestamp) {
    return <dd>n/a</dd>
  }

  return (
      <dd title={moment(timestamp).format()}>
        {moment(timestamp).fromNow()}
      </dd>
  )

}