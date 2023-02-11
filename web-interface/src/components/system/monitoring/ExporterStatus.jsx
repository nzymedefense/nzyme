import React from 'react'

function ExporterStatus (props) {
  if (!props || !props.status) {
    return <span>Disabled</span>
  } else {
    return <span>Enabled</span>
  }
}

export default ExporterStatus
