import React from 'react'
import Routes from '../../util/ApiRoutes'

function AlertField (props) {
  const key = props.fieldKey
  const value = props.value
  const fields = props.fields

  let additional
  // Link to bandit.
  if ((key === 'bandit_name' && 'bandit_uuid' in fields) || key === 'bandit_uuid') {
    additional = <a href={Routes.BANDITS.SHOW(fields.bandit_uuid)}><i className="fas fa-link" /></a>
  }

  // Link to network details if channel is known.
  if ('ssid' in fields && 'bssid' in fields && 'channel' in fields) {
    // We have all fields to link to a network. Link the right fields.
    if (key === 'ssid' || key === 'bssid') {
      additional = <a href={Routes.NETWORKS.SHOW(fields.bssid, fields.ssid, fields.channel)}><i className="fas fa-link" /></a>
    }
  }

  // Link to network details.
  if ('ssid' in fields && 'bssid' in fields && !('channel' in fields)) {
    // We have all fields to link to a network. Link the right fields.
    if (key === 'ssid' || key === 'bssid') {
      additional = <a href={Routes.NETWORKS.PROXY(fields.bssid, fields.ssid)}><i className="fas fa-link" /></a>
    }
  }

  return (
        <React.Fragment>
            <dt>{key}</dt>
            <dd>{value} {additional}</dd>
        </React.Fragment>
  )
}

export default AlertField
