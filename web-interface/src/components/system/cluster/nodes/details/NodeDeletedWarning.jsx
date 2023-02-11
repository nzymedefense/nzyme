import React from "react";

function NodeDeletedWarning(props) {

  if (!props.node || !props.node.deleted) {
    return null
  }

  return (
      <div className="alert alert-danger">
        <i className="fa-solid fa-triangle-exclamation"></i> This node has been manually deleted. It will be restored if
        it comes back online.
      </div>
  )

}

export default NodeDeletedWarning;