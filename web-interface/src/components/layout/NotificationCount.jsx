import React from 'react'

function NotificationCount (props) {
  if (!props.count) {
    return null
  }

  return <span className="badge rounded-pill bg-danger notification-count float-end">{props.count}</span>
}

export default NotificationCount
