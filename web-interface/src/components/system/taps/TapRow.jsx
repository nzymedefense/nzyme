import React from 'react'
import numeral from 'numeral'
import moment from 'moment'
import ApiRoutes from '../../../util/ApiRoutes'
import byteAverageToMbit from '../../../util/Tools'

function TapsRow (props) {

  const tap = props.tap
  const showOfflineTaps = props.showOfflineTaps;

  if (tap.active && !tap.deleted) {
    return (
      <tr>
        <td>
          <a href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>{tap.name}</a>

          {tap.active
            ? ''
            : <span>&nbsp;
              <i className="fa-solid fa-triangle-exclamation text-danger" title="Tap is offline." />
            </span>}
        </td>
        <td>{byteAverageToMbit(tap.processed_bytes.average)} ({numeral(tap.processed_bytes.average / 10).format('0 b')}/sec)</td>
        <td>{numeral(tap.processed_bytes.total).format('0.0 b')}</td>
        <td>{numeral(tap.cpu_load).format('0.0')}%</td>
        <td>
          {numeral(tap.memory_used).format('0 b')} / {numeral(tap.memory_total).format('0 b')} ({numeral(tap.memory_used / tap.memory_total * 100).format('0.0')}%)
        </td>
        <td>{tap.clock_drift_ms < -5000 || tap.clock_drift_ms > 5000
          ? <i className="fa-solid fa-warning text-danger" title="Clock drift detected"/>
          : <i className="fa-regular fa-circle-check" title="No clock drift detected" />}</td>
        <td title={moment(tap.updated_at).format()}>
          {moment(tap.updated_at).fromNow()}
        </td>
      </tr>
    )
  } else {
    if (showOfflineTaps) {
      if (tap.deleted) {
        return (
            <tr>
              <td><a href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>{tap.name}</a></td>
              <td colSpan={6} style={{textAlign: "center"}} title={moment(tap.updated_at).format()}>
                <span><i className="fa-solid fa-triangle-exclamation text-danger" title="Node has been deleted."/></span>{' '}
                Node has been manually deleted and will expire automatically if not brought back online
              </td>
            </tr>
        )
      } else {
        return (
            <tr>
              <td><a href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>{tap.name}</a></td>
              <td colSpan={6} style={{textAlign: "center"}} title={moment(tap.updated_at).format()}>
                <span><i className="fa-solid fa-triangle-exclamation text-danger" title="Node is offline."/></span>{' '}
                Last seen {moment(tap.updated_at).fromNow()} and will expire automatically if not brought back online
              </td>
            </tr>
        )
      }
    } else {
      return null
    }
  }

}

export default TapsRow
