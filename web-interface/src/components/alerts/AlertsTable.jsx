import React, { useState, useEffect } from 'react'

import LoadingSpinner from '../misc/LoadingSpinner'
import moment from 'moment'
import Routes from '../../util/ApiRoutes'
import FrameCount from './FrameCount'
import AlertSSID from './AlertSSID'

function alertRow (key, alert) {
  return (
    <tr key={key} className={alert.is_active ? 'text-danger' : 'text-warning'}>
      <td>{alert.id.substr(0, 8)}</td>
      <td>{alert.type}</td>
      <td><AlertSSID ssid={alert.fields.ssid} /></td>
      <td>{moment(alert.first_seen).fromNow()}</td>
      <td>{moment(alert.last_seen).fromNow()}</td>
      <td><FrameCount alert={alert} /></td>
      <td><a href={Routes.ALERTS.SHOW(alert.id)}>Details</a></td>
    </tr>
  )
}

function AlertsTable (props) {
  const [alerts, setAlerts] = useState()

  useEffect(() => {
    setAlerts(props.alerts)
  }, [props.alerts])

  if (!alerts) {
    return <LoadingSpinner />
  } else {
    if (alerts.length === 0) {
      return (
          <div className="alert alert-info">
            No alerts yet.
          </div>
      )
    }

    return (
      <div className="row">
        <div className="col-md-12">
          <table className="table table-sm table-hover table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Type</th>
                <th>Related SSID</th>
                <th>First Seen</th>
                <th>Last Seen</th>
                <th>Frames</th>
                <th>&nbsp;</th>
              </tr>
            </thead>
            <tbody>
            {Object.keys(alerts).map(function (key) {
              return (
                alertRow(key, alerts[key])
              )
            })}
            </tbody>
          </table>
        </div>
      </div>
    )
  }
}

export default AlertsTable
