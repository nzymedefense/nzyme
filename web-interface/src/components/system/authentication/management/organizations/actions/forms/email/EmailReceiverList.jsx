import React from "react";

function EmailReceiverList(props) {

  const receivers = props.receivers;
  const onDelete = props.onDelete;
  const readOnly = props.readOnly;

  if (!receivers || receivers.length === 0) {
    return <ul><li>No Receivers</li></ul>
  }

  return (
      <ul className="mb-0 mt-0">
        {receivers.sort().map((receiver, i) => {
          return (
              <li key={"receiver-" + i}>
                {receiver} {readOnly ? null : <a href="#" onClick={(e) => onDelete(e, receiver)}>Delete</a> }
              </li>
          )
        })}
      </ul>
  )

}

export default EmailReceiverList;