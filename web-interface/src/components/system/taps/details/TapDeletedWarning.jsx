import React from "react";

function TapDeletedWarning(props) {

  if (!props.tap || !props.tap.deleted) {
    return null
  }

  return (
      <div className="alert alert-danger">
        <i className="fa-solid fa-triangle-exclamation"></i> This tap has been manually deleted. It will be restored if
        it comes back online.
      </div>
  )

}

export default TapDeletedWarning;