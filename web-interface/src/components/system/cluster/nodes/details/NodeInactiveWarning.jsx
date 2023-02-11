import React from 'react'

function NodeInactiveWarning (props) {
  if (!props.node.active) {
    return (
        <div className="alert alert-danger">
          <i className="fa-solid fa-triangle-exclamation"></i> This node has not recently reported data and is
          offline. Some data you see on this page is likely outdated.
        </div>
    )
  } else {
    return null
  }
}

export default NodeInactiveWarning
