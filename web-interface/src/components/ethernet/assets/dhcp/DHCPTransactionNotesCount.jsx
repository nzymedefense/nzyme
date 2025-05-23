import React from "react";

export default function DHCPTransactionNotesCount(props) {

  const notes = props.notes;

  if (!notes || notes.length === 0) {
    return <span className="text-muted">None</span>;
  }

  return (
    <span className="text-danger">
      <i className="fa-solid fa-exclamation-triangle"></i> {notes.length}
    </span>
  )

}