import React from "react";

function EmailReceiverList(props) {

  const receivers = props.receivers;
  const onDelete = props.onDelete;

  if (!receivers || receivers.length === 0) {
    return <ul><li>No Receivers</li></ul>
  }

  return (
      <ul>
        {receivers.sort().map((receiver, i) => {
          return (
              <li key={"receiver-" + i}>
                {receiver} <a href="#" onClick={(e) => onDelete(e, receiver)}>Delete</a>
              </li>
          )
        })}
      </ul>
  )

}

export default EmailReceiverList;